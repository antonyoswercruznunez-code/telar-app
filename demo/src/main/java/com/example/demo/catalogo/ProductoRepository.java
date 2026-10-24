package com.example.demo.catalogo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProductoRepository {
    private final JdbcTemplate jdbc;

    public ProductoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> buscarTodos(String busqueda, boolean incluirInactivos) {
        String termino = "%" + (busqueda == null ? "" : busqueda.trim()) + "%";
        return jdbc.queryForList("""
                SELECT p.id_producto AS idProducto, p.nombre, p.descripcion, p.precio,
                       p.precio_mayorista AS precioMayorista, p.categoria, p.activo,
                       COUNT(DISTINCT dp.id_detalle_producto) AS variantes,
                       COALESCE(SUM(s.cantidad_disponible), 0) AS stockTotal
                FROM producto p
                LEFT JOIN detalle_producto dp ON dp.id_producto = p.id_producto
                LEFT JOIN stock s ON s.id_detalle_producto = dp.id_detalle_producto
                WHERE (p.nombre LIKE ? OR COALESCE(p.categoria, '') LIKE ?) AND (p.activo = 1 OR ? = 1)
                GROUP BY p.id_producto, p.nombre, p.descripcion, p.precio,
                         p.precio_mayorista, p.categoria, p.activo
                ORDER BY p.nombre
                """, termino, termino, incluirInactivos ? 1 : 0);
    }

    public Optional<Map<String, Object>> buscarPorId(int id) {
        List<Map<String, Object>> filas = jdbc.queryForList(
                "SELECT id_producto AS idProducto, nombre, descripcion, precio, precio_mayorista AS precioMayorista, cantidad_minima_mayorista AS cantidadMinimaMayorista, categoria FROM producto WHERE id_producto = ?",
                id);
        return filas.stream().findFirst();
    }

    public List<Map<String, Object>> buscarVariantes(int idProducto) {
        return jdbc.queryForList("""
                SELECT dp.id_detalle_producto AS idDetalleProducto, dp.color, dp.talla,
                       dp.material, COALESCE(SUM(s.cantidad_disponible), 0) AS disponible,
                       COALESCE(MAX(s.cantidad_minima), 0) AS minimo
                FROM detalle_producto dp
                LEFT JOIN stock s ON s.id_detalle_producto = dp.id_detalle_producto
                WHERE dp.id_producto = ?
                GROUP BY dp.id_detalle_producto, dp.color, dp.talla, dp.material
                ORDER BY dp.color, dp.talla
                """, idProducto);
    }

    public int cantidadProductos() {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM producto WHERE activo = 1", Integer.class);
        return total == null ? 0 : total;
    }

    public int crearProducto(ProductoForm producto) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO producto (nombre, descripcion, precio, precio_mayorista,
                                          cantidad_minima_mayorista, categoria)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, producto.getNombre().trim());
            ps.setString(2, vacioANull(producto.getDescripcion()));
            ps.setBigDecimal(3, producto.getPrecio());
            ps.setBigDecimal(4, producto.getPrecioMayorista());
            ps.setInt(5, producto.getCantidadMinimaMayorista() == null ? 12 : producto.getCantidadMinimaMayorista());
            ps.setString(6, vacioANull(producto.getCategoria()));
            return ps;
        }, key);
        return key.getKey().intValue();
    }

    public void actualizarProducto(int id, ProductoForm producto) {
        jdbc.update("""
                        UPDATE producto SET nombre = ?, descripcion = ?, precio = ?, precio_mayorista = ?,
                                            cantidad_minima_mayorista = ?, categoria = ?
                        WHERE id_producto = ?
                        """, producto.getNombre().trim(), vacioANull(producto.getDescripcion()),
                producto.getPrecio(), producto.getPrecioMayorista(),
                producto.getCantidadMinimaMayorista() == null ? 12 : producto.getCantidadMinimaMayorista(),
                vacioANull(producto.getCategoria()), id);
    }

    public int crearVariante(int idProducto, VarianteForm variante) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO detalle_producto (id_producto, color, talla, material) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, idProducto);
            ps.setString(2, variante.getColor().trim());
            ps.setString(3, variante.getTalla().trim());
            ps.setString(4, vacioANull(variante.getMaterial()));
            return ps;
        }, key);
        return key.getKey().intValue();
    }

    public void crearStock(int idDetalleProducto, VarianteForm variante) {
        jdbc.update("""
                        INSERT INTO stock (id_detalle_producto, cantidad_disponible, cantidad_minima)
                        VALUES (?, ?, ?)
                        """, idDetalleProducto,
                variante.getStockInicial() == null ? 0 : variante.getStockInicial(),
                variante.getStockMinimo() == null ? 0 : variante.getStockMinimo());
    }

    public boolean existe(int id) {
        Integer cantidad = jdbc.queryForObject("SELECT COUNT(*) FROM producto WHERE id_producto = ?", Integer.class, id);
        return cantidad != null && cantidad > 0;
    }

    /** ¿Ya hay otro producto con ese nombre (sin distinguir mayúsculas)? {@code idExcluido} = 0 al crear. */
    public boolean existeNombre(String nombre, int idExcluido) {
        Integer cantidad = jdbc.queryForObject(
                "SELECT COUNT(*) FROM producto WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(?)) AND id_producto <> ?",
                Integer.class, nombre, idExcluido);
        return cantidad != null && cantidad > 0;
    }

    /** @return filas afectadas (0 si el producto no existe) */
    public int cambiarEstado(int id, boolean activo) {
        return jdbc.update("UPDATE producto SET activo = ? WHERE id_producto = ?", activo ? 1 : 0, id);
    }

    private String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}