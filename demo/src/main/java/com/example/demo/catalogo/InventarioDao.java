package com.example.demo.catalogo;

/** Puerto de lectura de inventario. Un inventario vacío devuelve conteos cero. */
public interface InventarioDao {
    ResumenInventario consultar();
}
