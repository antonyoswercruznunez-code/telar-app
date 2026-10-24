package com.example.demo.dashboard;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** DAO de solo lectura con las consultas del dashboard. Usa parámetros (?) en todos los filtros. */
@Repository
public class DashboardRepository {
    private static final String CLIENTE =
            "COALESCE(NULLIF(c.razon_social, ''), TRIM(CONCAT_WS(' ', c.nombre, c.apellido)))";

    private final JdbcTemplate jdbc;

    public DashboardRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Ventas entre dos fechas, ambas incluidas. */
    public TotalVentas ventas(LocalDate desde, LocalDate hasta) {
        return jdbc.queryForObject("""
                SELECT COALESCE(SUM(total), 0) AS total, COUNT(*) AS cantidad
                FROM venta WHERE fecha_venta >= ? AND fecha_venta < ?
                """, (rs, n) -> new TotalVentas(rs.getBigDecimal("total"), rs.getInt("cantidad")),
                inicio(desde), inicio(hasta.plusDays(1)));
    }

    public Map<LocalDate, BigDecimal> ventasPorDia(LocalDate desde, LocalDate hasta) {
        return jdbc.query("""
                SELECT DATE(fecha_venta) AS dia, SUM(total) AS total
                FROM venta WHERE fecha_venta >= ? AND fecha_venta < ?
                GROUP BY DATE(fecha_venta)
                """, rs -> {
            Map<LocalDate, BigDecimal> dias = new HashMap<>();
            while (rs.next()) dias.put(rs.getDate("dia").toLocalDate(), rs.getBigDecimal("total"));
            return dias;
        }, inicio(desde), inicio(hasta.plusDays(1)));
    }

    public BigDecimal saldoPorCobrar() {
        BigDecimal saldo = jdbc.queryForObject(
                "SELECT COALESCE(SUM(saldo_pendiente), 0) FROM venta WHERE saldo_pendiente > 0", BigDecimal.class);
        return saldo == null ? BigDecimal.ZERO : saldo;
    }

    public int pedidosActivos() {
        return contar("SELECT COUNT(*) FROM pedido WHERE estado NOT IN ('Entregado', 'Cancelado')");
    }

    public int pedidosAtrasados() {
        return contar("SELECT COUNT(*) FROM pedido WHERE estado NOT IN ('Entregado', 'Cancelado') AND fecha_entrega < CURDATE()");
    }

    public int alertasStock() {
        return contar("""
                SELECT COUNT(*) FROM stock s
                JOIN detalle_producto dp ON dp.id_detalle_producto = s.id_detalle_producto
                JOIN producto p ON p.id_producto = dp.id_producto
                WHERE p.activo = 1 AND s.cantidad_disponible <= s.cantidad_minima
                """);
    }

    public int clientesActivos() {
        return contar("SELECT COUNT(*) FROM cliente WHERE activo = 1");
    }

    public Map<String, Integer> pedidosPorEstado() {
        return jdbc.query("SELECT estado, COUNT(*) AS cantidad FROM pedido GROUP BY estado", rs -> {
            Map<String, Integer> estados = new LinkedHashMap<>();
            while (rs.next()) estados.put(rs.getString("estado"), rs.getInt("cantidad"));
            return estados;
        });
    }

    /** Pedidos aún no entregados, los de entrega más cercana primero. */
    public List<Map<String, Object>> pedidosPorEntregar(int limite) {
        return jdbc.queryForList("""
                SELECT p.id_pedido AS id,
                """ + CLIENTE + """
                 AS cliente, p.estado,
                       DATE_FORMAT(p.fecha_entrega, '%d/%m/%Y') AS entrega,
                       CASE WHEN p.fecha_entrega < CURDATE() THEN 1 ELSE 0 END AS atrasado
                FROM pedido p JOIN cliente c ON c.id_cliente = p.id_cliente
                WHERE p.estado NOT IN ('Entregado', 'Cancelado')
                ORDER BY p.fecha_entrega IS NULL, p.fecha_entrega
                LIMIT ?
                """, limite);
    }

    public List<Map<String, Object>> stockBajo(int limite) {
        return jdbc.queryForList("""
                SELECT p.nombre AS producto, dp.color, dp.talla,
                       s.cantidad_disponible AS disponible, s.cantidad_minima AS minimo
                FROM stock s
                JOIN detalle_producto dp ON dp.id_detalle_producto = s.id_detalle_producto
                JOIN producto p ON p.id_producto = dp.id_producto
                WHERE p.activo = 1 AND s.cantidad_disponible <= s.cantidad_minima
                ORDER BY (s.cantidad_disponible - s.cantidad_minima), p.nombre
                LIMIT ?
                """, limite);
    }

    public List<Map<String, Object>> ultimasVentas(int limite) {
        return jdbc.queryForList("""
                SELECT v.id_venta AS id, DATE_FORMAT(v.fecha_venta, '%d/%m/%Y %H:%i') AS fecha,
                """ + CLIENTE + """
                 AS cliente, v.total, v.estado_pago AS estadoPago
                FROM venta v JOIN cliente c ON c.id_cliente = v.id_cliente
                ORDER BY v.fecha_venta DESC
                LIMIT ?
                """, limite);
    }

    private int contar(String sql) {
        Integer cantidad = jdbc.queryForObject(sql, Integer.class);
        return cantidad == null ? 0 : cantidad;
    }

    private static Timestamp inicio(LocalDate dia) {
        return Timestamp.valueOf(dia.atStartOfDay());
    }
}
