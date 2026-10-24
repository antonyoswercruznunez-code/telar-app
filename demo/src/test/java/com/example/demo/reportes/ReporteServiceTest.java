package com.example.demo.reportes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {
    @Mock
    ReporteRepository repository;

    private ReporteService servicio() {
        return new ReporteService(repository, new ReporteExcelService(), new ReportePdfService());
    }

    private TablaReporte tabla() {
        return new TablaReporte(List.of("N.º"), List.<List<Object>>of(List.<Object>of(1)));
    }

    @Test
    void ventasConFechaFinalAnteriorSeRechazaSinConsultar() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio().ventas(LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 1), FormatoReporte.XLSX));
        verifyNoInteractions(repository);
    }

    @Test
    void ventasConRangoMayorAUnAnioSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio().ventas(LocalDate.of(2024, 1, 1), LocalDate.of(2026, 1, 1), FormatoReporte.XLSX));
    }

    @Test
    void ventasValidasEnExcelConsultanElRepositorio() {
        LocalDate desde = LocalDate.of(2026, 10, 1), hasta = LocalDate.of(2026, 10, 20);
        when(repository.ventas(desde, hasta)).thenReturn(tabla());

        byte[] excel = servicio().ventas(desde, hasta, FormatoReporte.XLSX);

        verify(repository).ventas(desde, hasta);
        assertEquals('P', (char) excel[0]);   // los .xlsx son ZIP: empiezan con "PK"
        assertEquals('K', (char) excel[1]);
    }

    @Test
    void ventasEnPdfDevuelvenUnArchivoPdf() {
        LocalDate desde = LocalDate.of(2026, 10, 1), hasta = LocalDate.of(2026, 10, 20);
        when(repository.ventas(desde, hasta)).thenReturn(tabla());

        byte[] pdf = servicio().ventas(desde, hasta, FormatoReporte.PDF);

        assertEquals("%PDF", new String(pdf, 0, 4));
    }

    @Test
    void pedidosConEstadoInexistenteSeRechazaSinConsultar() {
        assertThrows(IllegalArgumentException.class, () -> servicio().pedidos("Volando", FormatoReporte.PDF));
        verifyNoInteractions(repository);
    }

    @Test
    void pedidosSinEstadoTraeTodos() {
        when(repository.pedidos("")).thenReturn(tabla());
        assertTrue(servicio().pedidos("", FormatoReporte.XLSX).length > 0);
    }
}
