package com.example.demo.seguridad;

import java.util.List;

/**
 * Qué roles pueden usar cada área (RF-25: administrador / personal de ventas). Es la única fuente de verdad:
 * la usan {@link SeguridadConfig} (bloqueo de URLs) y el menú lateral (qué opciones se muestran).
 */
public final class Permisos {
    private Permisos() { }

    /** Solo administrador: usuarios, compras e insumos, producción, stock, edición de productos, eliminar. */
    public static final List<String> ADMIN = List.of("ADMIN");
    /** Operación diaria: clientes, pedidos, ventas, pagos, caja, consulta de productos e inventario. */
    public static final List<String> TODOS = List.of("ADMIN", "VENDEDOR");

    // Claves de /gestion/{clave} (ver Entidad) agrupadas por área
    public static final List<String> CLAVES_TODOS = List.of(
            "pedido", "detalle_pedido", "medida_personalizada",
            "venta", "detalle_venta", "metodo_pago", "pago_venta", "comprobante", "cierre_caja", "reporte");
    public static final List<String> CLAVES_ADMIN = List.of(
            "stock", "proveedor", "compra", "detalle_compra", "conversion_unidad", "insumo",
            "orden_produccion", "detalle_orden_produccion", "ficha_tecnica");
}
