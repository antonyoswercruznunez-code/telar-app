package com.example.demo.comun;

/** Consultas reutilizables para los listados desplegables (columnas «id» y «etiqueta»). */
public final class Lookups {
    private Lookups() { }

    private static final String CLIENTE = "COALESCE(c.razon_social, CONCAT_WS(' ', c.nombre, c.apellido))";

    public static final String USUARIOS =
            "SELECT id_usuario AS id, nombre AS etiqueta FROM usuario ORDER BY nombre";
    public static final String CLIENTES =
            "SELECT id_cliente AS id, CONCAT_WS(' ', nombre, apellido, razon_social) AS etiqueta FROM cliente WHERE activo = 1 ORDER BY nombre";
    public static final String PRODUCTOS =
            "SELECT id_producto AS id, nombre AS etiqueta FROM producto WHERE activo = 1 ORDER BY nombre";
    public static final String VARIANTES =
            "SELECT dp.id_detalle_producto AS id, CONCAT(p.nombre, ' · ', dp.color, ' · ', dp.talla) AS etiqueta "
                    + "FROM detalle_producto dp JOIN producto p ON p.id_producto = dp.id_producto "
                    + "WHERE p.activo = 1 ORDER BY p.nombre, dp.color, dp.talla";
    public static final String PEDIDOS =
            "SELECT p.id_pedido AS id, CONCAT('Pedido #', p.id_pedido, ' · ', " + CLIENTE + ") AS etiqueta "
                    + "FROM pedido p JOIN cliente c ON c.id_cliente = p.id_cliente ORDER BY p.id_pedido DESC";
    public static final String DETALLES_PEDIDO =
            "SELECT d.id_detalle AS id, CONCAT('Pedido #', d.id_pedido, ' · ', pr.nombre, ' · ', dp.color, ' · ', dp.talla) AS etiqueta "
                    + "FROM detalle_pedido d JOIN detalle_producto dp ON dp.id_detalle_producto = d.id_detalle_producto "
                    + "JOIN producto pr ON pr.id_producto = dp.id_producto ORDER BY d.id_pedido DESC, d.id_detalle";
    public static final String VENTAS =
            "SELECT v.id_venta AS id, CONCAT('Venta #', v.id_venta, ' · ', " + CLIENTE + ", ' · ', v.total) AS etiqueta "
                    + "FROM venta v JOIN cliente c ON c.id_cliente = v.id_cliente ORDER BY v.id_venta DESC";
    public static final String METODOS_PAGO =
            "SELECT id_metodo_pago AS id, nombre AS etiqueta FROM metodo_pago ORDER BY nombre";
    public static final String ORDENES =
            "SELECT o.id_orden_produccion AS id, CONCAT('Orden #', o.id_orden_produccion, ' · ', p.nombre, ' · ', dp.color, ' · ', dp.talla) AS etiqueta "
                    + "FROM orden_produccion o JOIN detalle_producto dp ON dp.id_detalle_producto = o.id_detalle_producto "
                    + "JOIN producto p ON p.id_producto = dp.id_producto ORDER BY o.id_orden_produccion DESC";
    public static final String PROVEEDORES =
            "SELECT id_proveedor AS id, razon_social AS etiqueta FROM proveedor ORDER BY razon_social";
    public static final String INSUMOS =
            "SELECT id_insumo AS id, CONCAT(nombre, ' (', unidad_medida, ')') AS etiqueta FROM insumo ORDER BY nombre";
    public static final String COMPRAS =
            "SELECT c.id_compra AS id, CONCAT('Compra #', c.id_compra, ' · ', p.razon_social) AS etiqueta "
                    + "FROM compra c JOIN proveedor p ON p.id_proveedor = c.id_proveedor ORDER BY c.id_compra DESC";
}
