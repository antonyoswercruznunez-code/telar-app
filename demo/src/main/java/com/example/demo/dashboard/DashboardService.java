package com.example.demo.dashboard;

import com.example.demo.pedidos.EstadoPedido;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Arma el resumen del dashboard: completa con ceros los días y estados sin datos para que los gráficos no tengan huecos. */
@Service
public class DashboardService {
    static final int DIAS_GRAFICO = 7;
    static final int FILAS_LISTA = 6;
    private static final Locale ES = Locale.forLanguageTag("es-PE");

    private final DashboardRepository repository;
    private final Clock reloj;

    public DashboardService(DashboardRepository repository, Clock reloj) {
        this.repository = repository;
        this.reloj = reloj;
    }

    public ResumenDashboard resumen() {
        LocalDate hoy = LocalDate.now(reloj);
        TotalVentas ventasHoy = repository.ventas(hoy, hoy);
        TotalVentas ventasMes = repository.ventas(hoy.withDayOfMonth(1), hoy);

        return new ResumenDashboard(
                ventasHoy.total(), ventasHoy.cantidad(), ventasMes.total(), ventasMes.cantidad(),
                repository.saldoPorCobrar(), repository.pedidosActivos(), repository.pedidosAtrasados(),
                repository.alertasStock(), repository.clientesActivos(),
                ventasUltimosDias(hoy), pedidosPorEstado(),
                repository.pedidosPorEntregar(FILAS_LISTA), repository.stockBajo(FILAS_LISTA),
                repository.ultimasVentas(FILAS_LISTA));
    }

    /** Los últimos 7 días terminando hoy; los días sin ventas valen 0. */
    List<PuntoGrafico> ventasUltimosDias(LocalDate hoy) {
        Map<LocalDate, BigDecimal> porDia = repository.ventasPorDia(hoy.minusDays(DIAS_GRAFICO - 1L), hoy);
        List<PuntoGrafico> puntos = new ArrayList<>();
        for (int i = DIAS_GRAFICO - 1; i >= 0; i--) {
            LocalDate dia = hoy.minusDays(i);
            puntos.add(new PuntoGrafico(etiqueta(dia), porDia.getOrDefault(dia, BigDecimal.ZERO)));
        }
        return puntos;
    }

    /** Un punto por cada estado, en el orden del flujo (Recibido → … → Cancelado), aunque no tenga pedidos. */
    List<PuntoGrafico> pedidosPorEstado() {
        Map<String, Integer> cantidades = repository.pedidosPorEstado();
        return EstadoPedido.etiquetas().stream()
                .map(estado -> new PuntoGrafico(estado, BigDecimal.valueOf(cantidades.getOrDefault(estado, 0))))
                .toList();
    }

    private static String etiqueta(LocalDate dia) {
        return dia.getDayOfWeek().getDisplayName(TextStyle.SHORT, ES) + " " + dia.getDayOfMonth();
    }
}
