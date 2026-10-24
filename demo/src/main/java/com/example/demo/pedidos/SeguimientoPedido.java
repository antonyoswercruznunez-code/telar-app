package com.example.demo.pedidos;

import java.util.List;
import java.util.Map;

/** Pedido con todo lo que le cuelga: productos, órdenes de producción y ventas. */
public record SeguimientoPedido(Map<String, Object> pedido, String siguienteEstado,
                                List<Map<String, Object>> productos, List<Map<String, Object>> produccion,
                                List<Map<String, Object>> ventas) { }
