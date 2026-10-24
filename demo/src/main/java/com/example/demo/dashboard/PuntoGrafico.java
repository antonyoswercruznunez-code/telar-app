package com.example.demo.dashboard;

import java.math.BigDecimal;

/** Un valor etiquetado de un gráfico (por ejemplo, «sáb 24» → 150.00). */
public record PuntoGrafico(String etiqueta, BigDecimal valor) { }
