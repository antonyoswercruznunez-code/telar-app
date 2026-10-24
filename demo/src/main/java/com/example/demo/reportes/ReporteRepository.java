package com.example.demo.reportes;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** DAO de solo lectura: consultas que alimentan los reportes. Todas las consultas usan parámetros (?). */
@Repository
public class ReporteRepository {
    private final JdbcTemplate jdbc;

    public ReporteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public TablaReporte inventario() {
        return consultar("""
                SELECT p.nombre AS `Producto`, COALESCE(p.categoria, '') AS `Categoría`,
                       dp.color AS `Color`, dp.talla AS `Talla`, COALESCE(dp.material, '') AS `Material`,
                       COALESCE(SUM(s.cantidad_disponible), 0) AS `Stock`,
                       COALESCE(MAX(s.cantidad_minima), 0) AS `Stock mínimo`,
                       CASE WHEN COALESCE(SUM(s.cantidad_disponible), 0) <= COALESCE(MAX(s.cantidad_minima), 0)
                            THEN 'Reponer' ELSE 'OK' END AS `Estado`
                FROM producto p
                JOIN detalle_producto dp ON dp.id_producto = p.id_producto
                LEFT JOIN stock s ON s.id_detalle_producto = dp.id_detalle_producto
                WHERE p.activo = 1
                GROUP BY p.id_producto, p.nombre, p.categoria, dp.id_detalle_producto, dp.color, dp.talla, dp.material
                ORDER BY p.nombre, dp.color, dp.talla
                """);
    }

    public TablaReporte ventas(LocalDate desde, LocalDate hasta) {
        return consultar("""
                SELECT v.id_venta AS `N.º`, v.fecha_venta AS `Fecha`,
                       COALESCE(NULLIF(c.razon_social, ''), TRIM(CONCAT(c.nombre, ' ', COALESCE(c.apellido, '')))) AS `Cliente`,
                       v.tipo_venta AS `Tipo`, v.total AS `Total`, v.monto_adelanto AS `Adelanto`,
                       v.saldo_pendiente AS `Saldo pendiente`, v.estado_pago AS `Estado de pago`
                FROM venta v
                JOIN cliente c ON c.id_cliente = v.id_cliente
                WHERE v.fecha_venta >= ? AND v.fecha_venta < ?
                ORDER BY v.fecha_venta DESC
                """, Timestamp.valueOf(desde.atStartOfDay()), Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
    }

    /** Pedidos y su estado (RF-21). {@code estado} vacío o null = todos. */
    public TablaReporte pedidos(String estado) {
        String filtro = estado == null ? "" : estado;
        return consultar("""
                SELECT p.id_pedido AS `N.º`, DATE_FORMAT(p.fecha_pedido, '%d/%m/%Y') AS `Fecha`,
                       COALESCE(NULLIF(c.razon_social, ''), TRIM(CONCAT_WS(' ', c.nombre, c.apellido))) AS `Cliente`,
                       COALESCE(p.descripcion, '') AS `Descripción`, p.estado AS `Estado`,
                       COALESCE(DATE_FORMAT(p.fecha_entrega, '%d/%m/%Y'), '') AS `Entrega`
                FROM pedido p
                JOIN cliente c ON c.id_cliente = p.id_cliente
                WHERE (? = '' OR p.estado = ?)
                ORDER BY p.fecha_pedido DESC
                """, filtro, filtro);
    }

    public TablaReporte insumosBajos() {
        return consultar("""
                SELECT nombre AS `Insumo`, unidad_medida AS `Unidad`,
                       cantidad_disponible AS `Disponible`, cantidad_minima AS `Mínimo`
                FROM insumo
                WHERE cantidad_disponible <= cantidad_minima
                ORDER BY nombre
                """);
    }

    private TablaReporte consultar(String sql, Object... args) {
        return jdbc.query(sql, rs -> {
            ResultSetMetaData md = rs.getMetaData();
            int n = md.getColumnCount();
            List<String> columnas = new ArrayList<>();
            for (int i = 1; i <= n; i++) columnas.add(md.getColumnLabel(i));
            List<List<Object>> filas = new ArrayList<>();
            while (rs.next()) {
                List<Object> fila = new ArrayList<>(n);
                for (int i = 1; i <= n; i++) fila.add(rs.getObject(i));
                filas.add(fila);
            }
            return new TablaReporte(columnas, filas);
        }, args);
    }
}
