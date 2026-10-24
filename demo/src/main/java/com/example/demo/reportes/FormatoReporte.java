package com.example.demo.reportes;

import org.apache.commons.lang3.StringUtils;

/** Formatos de exportación de los reportes. */
public enum FormatoReporte {
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PDF("pdf", "application/pdf");

    private final String extension;
    private final String tipoContenido;

    FormatoReporte(String extension, String tipoContenido) {
        this.extension = extension;
        this.tipoContenido = tipoContenido;
    }

    public String extension() { return extension; }

    public String tipoContenido() { return tipoContenido; }

    /** Texto vacío o null = Excel. */
    public static FormatoReporte de(String texto) {
        if (StringUtils.isBlank(texto)) return XLSX;
        for (FormatoReporte f : values()) {
            if (f.extension.equalsIgnoreCase(texto.trim())) return f;
        }
        throw new IllegalArgumentException("Formato no válido. Usa xlsx o pdf.");
    }
}
