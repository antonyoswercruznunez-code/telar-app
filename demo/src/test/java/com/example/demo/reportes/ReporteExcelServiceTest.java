package com.example.demo.reportes;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReporteExcelServiceTest {
    private final ReporteExcelService servicio = new ReporteExcelService();

    @Test
    void generaEncabezadosYFilasConSusTipos() throws Exception {
        TablaReporte tabla = new TablaReporte(
                List.of("Producto", "Total", "Fecha", "Nota"),
                List.<List<Object>>of(Arrays.asList("Camisa", new BigDecimal("59.90"),
                        Timestamp.valueOf(LocalDateTime.of(2026, 10, 20, 9, 30)), null)));

        byte[] datos = servicio.generar("Ventas", tabla);

        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(datos))) {
            Sheet hoja = libro.getSheet("Ventas");
            assertEquals("Producto", hoja.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Camisa", hoja.getRow(1).getCell(0).getStringCellValue());
            assertEquals(59.90, hoja.getRow(1).getCell(1).getNumericCellValue(), 0.001);
            assertEquals("2026-10-20 09:30", hoja.getRow(1).getCell(2).getStringCellValue());
            assertNull(hoja.getRow(1).getCell(3));
        }
    }

    @Test
    void sinColumnasSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.generar("Vacío", new TablaReporte(List.of(), List.of())));
    }
}
