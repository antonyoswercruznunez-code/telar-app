package com.example.demo.pedidos;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** DAO de consulta de pedidos para el seguimiento. Los filtros se arman con parámetros (?). */
@Repository
public class PedidoSeguimientoRepository {
    private static final String CLIENTE =
            "COALESCE(NULLIF(c.razon_social, ''), TRIM(CONCAT_WS(' ', c.nombre, c.apellido)))";

    private final JdbcTemplate jdbc;

    public PedidoSeguimientoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> listar(String estado, LocalDate desde, LocalDate hasta, String texto) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.id_pedido AS id, DATE_FORMAT(p.fecha_pedido, '%d/%m/%Y') AS fecha,
                """ + CLIENTE + """
                 AS cliente, p.descripcion, p.estado,
                       DATE_FORMAT(p.fecha_entrega, '%d/%m/%Y') AS entrega,
                       CASE WHEN p.fecha_entrega < CURDATE() AND p.estado NOT IN ('Entregado', 'Cancelado')
                            THEN 1 ELSE 0 END AS atrasado
                FROM pedido p JOIN cliente c ON c.id_cliente = p.id_cliente
                WHERE CONCAT_WS(' ', c.nombre, c.apellido, c.razon_social, COALESCE(p.descripcion, '')) LIKE ?
                """);
        List<Object> args = new ArrayList<>();
        args.add("%" + (texto == null ? "" : texto.trim()) + "%");
        if (estado != null && !estado.isBlank()) {
            sql.append(" AND p.estado = ?");
            args.add(estado);
        }
        if (desde != null) {
            sql.append(" AND p.fecha_pedido >= ?");
            args.add(Timestamp.valueOf(desde.atStartOfDay()));
        }
        if (hasta != null) {
            sql.append(" AND p.fecha_pedido < ?");
            args.add(Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
        }
        sql.append(" ORDER BY p.fecha_pedido DESC LIMIT 500");
        return jdbc.queryForList(sql.toString(), args.toArray());
    }

    public Optional<Map<String, Object>> buscarPorId(int id) {
        return jdbc.queryForList("""
                SELECT p.id_pedido AS id, DATE_FORMAT(p.fecha_pedido, '%d/%m/%Y %H:%i') AS fecha,
                """ + CLIENTE + """
                 AS cliente, c.id_cliente AS idCliente, p.descripcion, p.estado,
                       DATE_FORMAT(p.fecha_entrega, '%d/%m/%Y') AS entrega,
                       CASE WHEN p.fecha_entrega < CURDATE() AND p.estado NOT IN ('Entregado', 'Cancelado')
                            THEN 1 ELSE 0 END AS atrasado
                FROM pedido p JOIN cliente c ON c.id_cliente = p.id_cliente
                WHERE p.id_pedido = ?
                """, id).stream().findFirst();
    }

    public Optional<String> estadoDe(int id) {
        return jdbc.queryForList("SELECT estado FROM pedido WHERE id_pedido = ?", String.class, id)
                .stream().findFirst();
    }

    /** @return filas afectadas (0 si el pedido no existe) */
    public int cambiarEstado(int id, String estado) {
        return jdbc.update("UPDATE pedido SET estado = ? WHERE id_pedido = ?", estado, id);
    }

    public List<Map<String, Object>> productosDe(int idPedido) {
        return jdbc.queryForList("""
                SELECT pr.nombre AS producto, dp.color, dp.talla, d.cantidad, d.subtotal
                FROM detalle_pedido d
                JOIN detalle_producto dp ON dp.id_detalle_producto = d.id_detalle_producto
                JOIN producto pr ON pr.id_producto = dp.id_producto
                WHERE d.id_pedido = ? ORDER BY d.id_detalle
                """, idPedido);
    }

    public List<Map<String, Object>> produccionDe(int idPedido) {
        return jdbc.queryForList("""
                SELECT o.id_orden_produccion AS id, pr.nombre AS producto, dp.color, dp.talla,
                       o.cantidad_a_producir AS cantidad, o.estado_fase AS fase,
                       DATE_FORMAT(o.fecha_inicio, '%d/%m/%Y') AS inicio,
                       DATE_FORMAT(o.fecha_fin_estimada, '%d/%m/%Y') AS fin
                FROM orden_produccion o
                JOIN detalle_producto dp ON dp.id_detalle_producto = o.id_detalle_producto
                JOIN producto pr ON pr.id_producto = dp.id_producto
                WHERE o.id_pedido = ? ORDER BY o.id_orden_produccion
                """, idPedido);
    }

    public List<Map<String, Object>> ventasDe(int idPedido) {
        return jdbc.queryForList("""
                SELECT id_venta AS id, DATE_FORMAT(fecha_venta, '%d/%m/%Y %H:%i') AS fecha,
                       total, saldo_pendiente AS saldo, estado_pago AS estadoPago
                FROM venta WHERE id_pedido = ? ORDER BY fecha_venta
                """, idPedido);
    }
}
