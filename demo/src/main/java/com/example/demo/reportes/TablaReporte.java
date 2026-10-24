package com.example.demo.reportes;

import java.util.List;

/** Resultado tabular de una consulta de reporte: encabezados y filas. */
public record TablaReporte(List<String> columnas, List<List<Object>> filas) { }
