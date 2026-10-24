package com.example.demo.catalogo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class InventarioController {
    private final JdbcTemplate jdbcTemplate;

    public InventarioController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/inventario")
    public String inventario(Model model) {
        List<Map<String, Object>> articulos = jdbcTemplate.queryForList("""
                SELECT p.id_producto AS idProducto,
                       p.nombre AS nombre,
                       p.categoria AS categoria,
                       p.precio AS precio,
                       dp.color AS color,
                       dp.talla AS talla,
                       dp.material AS material,
                       COALESCE(SUM(s.cantidad_disponible), 0) AS stockDisponible,
                       COALESCE(MAX(s.cantidad_minima), 0) AS stockMinimo
                FROM producto p
                LEFT JOIN detalle_producto dp ON dp.id_producto = p.id_producto
                LEFT JOIN stock s ON s.id_detalle_producto = dp.id_detalle_producto
                WHERE p.activo = 1
                GROUP BY p.id_producto, p.nombre, p.categoria, p.precio,
                         dp.id_detalle_producto, dp.color, dp.talla, dp.material
                ORDER BY p.nombre, dp.talla, dp.color
                """);

        Integer totalProductos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM producto WHERE activo = 1", Integer.class);
        Integer totalVariantes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM detalle_producto dp JOIN producto p ON p.id_producto = dp.id_producto WHERE p.activo = 1", Integer.class);
        Integer alertasStock = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock s JOIN detalle_producto dp ON dp.id_detalle_producto = s.id_detalle_producto JOIN producto p ON p.id_producto = dp.id_producto WHERE p.activo = 1 AND s.cantidad_disponible <= s.cantidad_minima",
                Integer.class);

        model.addAttribute("articulos", articulos);
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalVariantes", totalVariantes);
        model.addAttribute("alertasStock", alertasStock);
        return "catalogo/inventario";
    }
}
