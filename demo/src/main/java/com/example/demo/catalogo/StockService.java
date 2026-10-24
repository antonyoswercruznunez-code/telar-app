package com.example.demo.catalogo;

import com.example.demo.comun.ValidacionException;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/** Movimientos de existencias por variante (detalle_producto). */
@Service
public class StockService {
    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final JdbcTemplate jdbc;

    public StockService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public int disponible(int idDetalleProducto) {
        Integer total = jdbc.queryForObject(
                "SELECT COALESCE(SUM(cantidad_disponible), 0) FROM stock WHERE id_detalle_producto = ?",
                Integer.class, idDetalleProducto);
        return total == null ? 0 : total;
    }

    public void agregar(int idDetalleProducto, int cantidad) {
        Preconditions.checkArgument(idDetalleProducto > 0, "Variante de producto inválida.");
        if (cantidad <= 0) return;
        log.info("Stock +{} en variante {}", cantidad, idDetalleProducto);
        int filas = jdbc.update("UPDATE stock SET cantidad_disponible = cantidad_disponible + ? "
                + "WHERE id_stock = (SELECT id_stock FROM (SELECT MIN(id_stock) AS id_stock FROM stock WHERE id_detalle_producto = ?) t)",
                cantidad, idDetalleProducto);
        if (filas == 0) {
            jdbc.update("INSERT INTO stock (id_detalle_producto, cantidad_disponible, cantidad_minima) VALUES (?, ?, 0)",
                    idDetalleProducto, cantidad);
        }
    }

    public void descontar(int idDetalleProducto, int cantidad) {
        Preconditions.checkArgument(idDetalleProducto > 0, "Variante de producto inválida.");
        if (cantidad <= 0) return;
        if (disponible(idDetalleProducto) < cantidad) {
            log.warn("Stock insuficiente en variante {}: se pidieron {}", idDetalleProducto, cantidad);
            throw new ValidacionException("Stock insuficiente para «" + etiqueta(idDetalleProducto)
                    + "»: hay " + disponible(idDetalleProducto) + " y se piden " + cantidad + ".");
        }
        log.info("Stock -{} en variante {}", cantidad, idDetalleProducto);
        int pendiente = cantidad;
        List<Map<String, Object>> filas = jdbc.queryForList(
                "SELECT id_stock, cantidad_disponible FROM stock WHERE id_detalle_producto = ? ORDER BY id_stock", idDetalleProducto);
        for (Map<String, Object> f : filas) {
            if (pendiente == 0) break;
            int idStock = ((Number) f.get("id_stock")).intValue();
            int hay = ((Number) f.get("cantidad_disponible")).intValue();
            int quitar = Math.min(hay, pendiente);
            if (quitar > 0) {
                jdbc.update("UPDATE stock SET cantidad_disponible = cantidad_disponible - ? WHERE id_stock = ?", quitar, idStock);
                pendiente -= quitar;
            }
        }
    }

    public String etiqueta(int idDetalleProducto) {
        List<String> r = jdbc.queryForList(
                "SELECT CONCAT(p.nombre, ' · ', dp.color, ' · ', dp.talla) FROM detalle_producto dp "
                        + "JOIN producto p ON p.id_producto = dp.id_producto WHERE dp.id_detalle_producto = ?",
                String.class, idDetalleProducto);
        return r.isEmpty() ? "#" + idDetalleProducto : r.get(0);
    }
}
