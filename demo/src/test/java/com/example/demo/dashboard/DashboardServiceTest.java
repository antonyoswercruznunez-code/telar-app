package com.example.demo.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    private static final LocalDate HOY = LocalDate.of(2026, 10, 24);

    @Mock
    DashboardRepository repository;

    private DashboardService servicio;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(Instant.parse("2026-10-24T15:00:00Z"), ZoneId.of("UTC"));
        servicio = new DashboardService(repository, reloj);
        lenient().when(repository.ventas(HOY, HOY)).thenReturn(new TotalVentas(new BigDecimal("150.50"), 2));
        lenient().when(repository.ventas(LocalDate.of(2026, 10, 1), HOY)).thenReturn(new TotalVentas(new BigDecimal("900.00"), 9));
        lenient().when(repository.saldoPorCobrar()).thenReturn(new BigDecimal("75.00"));
    }

    @Test
    void elGraficoSemanalSiempreTieneSieteDiasYCompletaConCeros() {
        lenient().when(repository.ventasPorDia(LocalDate.of(2026, 10, 18), HOY)).thenReturn(Map.of(
                LocalDate.of(2026, 10, 22), new BigDecimal("100.00"),
                HOY, new BigDecimal("50.50")));

        List<PuntoGrafico> dias = servicio.ventasUltimosDias(HOY);

        assertEquals(7, dias.size());
        assertEquals(BigDecimal.ZERO, dias.get(0).valor());                  // 18 de octubre, sin ventas
        assertEquals(new BigDecimal("100.00"), dias.get(4).valor());         // 22 de octubre
        assertEquals(new BigDecimal("50.50"), dias.get(6).valor());          // hoy, último punto
    }

    @Test
    void losPedidosPorEstadoIncluyenTodosLosEstadosEnOrden() {
        lenient().when(repository.pedidosPorEstado()).thenReturn(Map.of("En proceso", 3, "Entregado", 1));

        List<PuntoGrafico> estados = servicio.pedidosPorEstado();

        assertEquals(5, estados.size());
        assertEquals("Recibido", estados.get(0).etiqueta());
        assertEquals(BigDecimal.ZERO, estados.get(0).valor());
        assertEquals("En proceso", estados.get(1).etiqueta());
        assertEquals(BigDecimal.valueOf(3), estados.get(1).valor());
        assertEquals("Cancelado", estados.get(4).etiqueta());
    }

    @Test
    void elResumenReunePorSeparadoLasVentasDeHoyYDelMes() {
        ResumenDashboard resumen = servicio.resumen();

        assertEquals(new BigDecimal("150.50"), resumen.ventasHoy());
        assertEquals(2, resumen.cantidadVentasHoy());
        assertEquals(new BigDecimal("900.00"), resumen.ventasMes());
        assertEquals(9, resumen.cantidadVentasMes());
        assertEquals(new BigDecimal("75.00"), resumen.saldoPorCobrar());
        assertEquals(7, resumen.ventasUltimosDias().size());
    }
}
