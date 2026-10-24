package com.example.demo.clientes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Pedidos y ventas de un cliente con sus totales (RF-04). */
public record HistorialCliente(ClienteForm cliente, List<Map<String, Object>> pedidos,
                               List<Map<String, Object>> ventas, BigDecimal totalComprado, BigDecimal saldoPendiente) { }
