package com.example.demo.reportes;

import com.google.common.base.Preconditions;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Convierte una {@link TablaReporte} en un PDF (RF-23). No conoce la base de datos. */
@Service
public class ReportePdfService {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generar(String titulo, TablaReporte tabla) {
        Preconditions.checkArgument(tabla != null, "La tabla del reporte es obligatoria.");
        Preconditions.checkArgument(!tabla.columnas().isEmpty(), "El reporte necesita al menos una columna.");

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            documento.add(new Paragraph("Telar MX · " + titulo, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            documento.add(new Paragraph("Generado el " + LocalDateTime.now().format(FECHA)
                    + " · " + tabla.filas().size() + " registros", FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY)));
            documento.add(new Paragraph(" "));

            Font fuenteEncabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font fuenteCelda = FontFactory.getFont(FontFactory.HELVETICA, 9);

            PdfPTable pdf = new PdfPTable(tabla.columnas().size());
            pdf.setWidthPercentage(100);
            pdf.setHeaderRows(1);
            for (String columna : tabla.columnas()) {
                PdfPCell celda = new PdfPCell(new Phrase(columna, fuenteEncabezado));
                celda.setBackgroundColor(new Color(230, 230, 230));
                celda.setPadding(4);
                pdf.addCell(celda);
            }
            for (var fila : tabla.filas()) {
                for (int c = 0; c < tabla.columnas().size(); c++) {
                    Object valor = c < fila.size() ? fila.get(c) : null;
                    PdfPCell celda = new PdfPCell(new Phrase(texto(valor), fuenteCelda));
                    celda.setPadding(3);
                    if (valor instanceof Number) celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    pdf.addCell(celda);
                }
            }
            documento.add(pdf);
            if (tabla.filas().isEmpty()) {
                documento.add(new Paragraph("No hay datos para mostrar.", fuenteCelda));
            }
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF.", e);
        } finally {
            if (documento.isOpen()) documento.close();
        }
        return salida.toByteArray();
    }

    private String texto(Object valor) {
        if (valor == null) return "";
        if (valor instanceof BigDecimal b) return b.setScale(2, RoundingMode.HALF_UP).toPlainString();
        if (valor instanceof Double || valor instanceof Float) return String.format(java.util.Locale.US, "%.2f", ((Number) valor).doubleValue());
        if (valor instanceof Timestamp t) return t.toLocalDateTime().format(FECHA);
        if (valor instanceof LocalDateTime d) return d.format(FECHA);
        return valor.toString();
    }
}
