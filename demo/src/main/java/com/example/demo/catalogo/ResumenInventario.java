package com.example.demo.catalogo;

import java.util.List;
import java.util.Map;

public record ResumenInventario(List<Map<String, Object>> articulos,
                               int totalProductos, int totalVariantes, int alertasStock) {
}
