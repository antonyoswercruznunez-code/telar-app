package com.example.demo.reportes;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Convierte una {@link TablaReporte} en un archivo .xlsx. No conoce la base de datos. */
@Service
public class ReporteExcelService {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] generar(String titulo, TablaReporte tabla) {
        Preconditions.checkArgument(tabla != null, "La tabla del reporte es obligatoria.");
        Preconditions.checkArgument(!tabla.columnas().isEmpty(), "El reporte necesita al menos una columna.");

        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet(WorkbookUtil.createSafeSheetName(StringUtils.defaultIfBlank(titulo, "Reporte")));

            Font negrita = libro.createFont();
            negrita.setBold(true);
            CellStyle estiloEncabezado = libro.createCellStyle();
            estiloEncabezado.setFont(negrita);

            Row encabezado = hoja.createRow(0);
            for (int c = 0; c < tabla.columnas().size(); c++) {
                encabezado.createCell(c).setCellValue(tabla.columnas().get(c));
                encabezado.getCell(c).setCellStyle(estiloEncabezado);
            }

            int r = 1;
            for (var fila : tabla.filas()) {
                Row row = hoja.createRow(r++);
                for (int c = 0; c < fila.size(); c++) escribir(row, c, fila.get(c));
            }
            for (int c = 0; c < tabla.columnas().size(); c++) hoja.autoSizeColumn(c);
            hoja.createFreezePane(0, 1);

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el archivo Excel.", e);
        }
    }

    private void escribir(Row row, int columna, Object valor) {
        if (valor == null) return;
        if (valor instanceof Number n) {
            row.createCell(columna).setCellValue(n.doubleValue());
        } else if (valor instanceof Timestamp t) {
            row.createCell(columna).setCellValue(t.toLocalDateTime().format(FECHA));
        } else if (valor instanceof LocalDateTime d) {
            row.createCell(columna).setCellValue(d.format(FECHA));
        } else {
            row.createCell(columna).setCellValue(valor.toString());
        }
    }
}
