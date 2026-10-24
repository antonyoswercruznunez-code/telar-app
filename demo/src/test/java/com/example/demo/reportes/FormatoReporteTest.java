package com.example.demo.reportes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormatoReporteTest {
    @Test
    void sinFormatoSeUsaExcel() {
        assertEquals(FormatoReporte.XLSX, FormatoReporte.de(null));
        assertEquals(FormatoReporte.XLSX, FormatoReporte.de(" "));
    }

    @Test
    void reconoceXlsxYPdfSinImportarMayusculas() {
        assertEquals(FormatoReporte.XLSX, FormatoReporte.de("XLSX"));
        assertEquals(FormatoReporte.PDF, FormatoReporte.de("pdf"));
    }

    @Test
    void formatoDesconocidoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> FormatoReporte.de("docx"));
    }
}
