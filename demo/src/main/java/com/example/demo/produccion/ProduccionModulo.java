package com.example.demo.produccion;

import com.example.demo.catalogo.StockService;
import com.example.demo.comun.Campo;
import com.example.demo.comun.Entidad;
import com.example.demo.comun.Lookups;
import com.example.demo.comun.ModuloCrud;
import com.example.demo.comun.ValidacionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.demo.comun.Util.dec;
import static com.example.demo.comun.Util.ent;
import static com.example.demo.comun.Util.entOpt;
import static com.example.demo.comun.Util.str;

/**
 * Módulo Producción.
 * Al crear una orden se calculan los insumos estimados con la ficha técnica (consumo × cantidad + merma).
 * Al pasar a «Terminado» se descuentan los insumos y se suma el producto terminado al stock.
 */
@Component
public class ProduccionModulo implements ModuloCrud {
    private static final String M = "Producción";
    private static final String TERMINADO = "Terminado";
    private final JdbcTemplate jdbc;
    private final StockService stock;

    public ProduccionModulo(JdbcTemplate jdbc, StockService stock) {
        this.jdbc = jdbc;
        this.stock = stock;
    }

    @Override
    public List<Entidad> entidades() {
        return List.of(
                Entidad.de(M, "orden_produccion", "Órdenes de producción", "Orden de producción", "orden_produccion", "id_orden_produccion",
                        Campo.fk("id_pedido", "Pedido", Lookups.PEDIDOS),
                        Campo.fk("id_detalle_producto", "Producto (variante)", Lookups.VARIANTES).req(),
                        Campo.fk("id_usuario", "Responsable", Lookups.USUARIOS).req().def("usuario"),
                        Campo.entero("cantidad_a_producir", "Cantidad a producir").req().def("1"),
                        Campo.fecha("fecha_inicio", "Fecha de inicio").req().def("hoy"),
                        Campo.fecha("fecha_fin_estimada", "Fin estimado"),
                        Campo.lista("estado_fase", "Fase", "En Corte", "En Confección", "En Acabado", TERMINADO, "Cancelado").req().def("En Corte"))
                        .hijo("Insumos", "detalle_orden_produccion", "id_orden_produccion"),
                Entidad.de(M, "detalle_orden_produccion", "Detalle de orden", "Insumo de la orden", "detalle_orden_produccion", "id_detalle_op",
                        Campo.fk("id_orden_produccion", "Orden", Lookups.ORDENES).req(),
                        Campo.fk("id_insumo", "Insumo", Lookups.INSUMOS).req(),
                        Campo.decimal("cantidad_estimada", "Cantidad estimada").req(),
                        Campo.decimal("cantidad_real_usada", "Cantidad real usada")),
                Entidad.de(M, "ficha_tecnica", "Fichas técnicas", "Ficha técnica", "ficha_tecnica", "id_ficha",
                        Campo.fk("id_detalle_producto", "Producto (variante)", Lookups.VARIANTES).req(),
                        Campo.fk("id_insumo", "Insumo", Lookups.INSUMOS).req(),
                        Campo.decimal("cantidad_consumo", "Consumo por unidad").req(),
                        Campo.decimal("porcentaje_merma", "Merma (%)").req().def("0")));
    }

    @Override
    public void antes(Entidad e, Map<String, Object> v, Map<String, Object> anterior) {
        switch (e.clave()) {
            case "orden_produccion" -> {
                if (anterior != null && TERMINADO.equals(str(anterior.get("estado_fase")))) {
                    throw new ValidacionException("La orden ya está terminada y no se puede modificar.");
                }
                if (ent(v.get("cantidad_a_producir")) <= 0) throw new ValidacionException("La cantidad a producir debe ser mayor que cero.");
                Date ini = (Date) v.get("fecha_inicio");
                Date fin = (Date) v.get("fecha_fin_estimada");
                if (fin != null && fin.before(ini)) throw new ValidacionException("El fin estimado no puede ser anterior al inicio.");
            }
            case "detalle_orden_produccion" -> {
                if (ordenTerminada(ent(v.get("id_orden_produccion")))) {
                    throw new ValidacionException("La orden está terminada: sus insumos ya no se pueden cambiar.");
                }
            }
            default -> { }
        }
    }

    @Override
    public void despues(Entidad e, int id, Map<String, Object> v, Map<String, Object> anterior) {
        if (!"orden_produccion".equals(e.clave())) return;
        int variante = ent(v.get("id_detalle_producto"));
        int cantidad = ent(v.get("cantidad_a_producir"));
        boolean cambio = anterior == null || ent(anterior.get("id_detalle_producto")) != variante
                || ent(anterior.get("cantidad_a_producir")) != cantidad;
        if (cambio) {
            jdbc.update("DELETE FROM detalle_orden_produccion WHERE id_orden_produccion = ? AND cantidad_real_usada IS NULL", id);
            generarInsumos(id, variante, cantidad);
        }
        if (TERMINADO.equals(str(v.get("estado_fase")))) terminar(id, variante, cantidad, entOpt(v.get("id_pedido")));
    }

    @Override
    public void antesEliminar(Entidad e, Map<String, Object> anterior) {
        if ("orden_produccion".equals(e.clave()) && TERMINADO.equals(str(anterior.get("estado_fase")))) {
            throw new ValidacionException("No se puede eliminar una orden terminada.");
        }
        if ("detalle_orden_produccion".equals(e.clave()) && ordenTerminada(ent(anterior.get("id_orden_produccion")))) {
            throw new ValidacionException("La orden está terminada: sus insumos ya no se pueden eliminar.");
        }
    }

    private boolean ordenTerminada(int idOrden) {
        List<String> r = jdbc.queryForList("SELECT estado_fase FROM orden_produccion WHERE id_orden_produccion = ?", String.class, idOrden);
        return !r.isEmpty() && TERMINADO.equals(r.get(0));
    }

    private void generarInsumos(int idOrden, int variante, int cantidad) {
        for (Map<String, Object> f : jdbc.queryForList(
                "SELECT id_insumo, cantidad_consumo, porcentaje_merma FROM ficha_tecnica WHERE id_detalle_producto = ?", variante)) {
            int insumo = ent(f.get("id_insumo"));
            Integer existe = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM detalle_orden_produccion WHERE id_orden_produccion = ? AND id_insumo = ?",
                    Integer.class, idOrden, insumo);
            if (existe != null && existe > 0) continue;
            BigDecimal factorMerma = BigDecimal.ONE.add(dec(f.get("porcentaje_merma")).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
            BigDecimal estimada = dec(f.get("cantidad_consumo")).multiply(BigDecimal.valueOf(cantidad)).multiply(factorMerma)
                    .setScale(2, RoundingMode.HALF_UP);
            jdbc.update("INSERT INTO detalle_orden_produccion (id_orden_produccion, id_insumo, cantidad_estimada) VALUES (?, ?, ?)",
                    idOrden, insumo, estimada);
        }
    }

    private void terminar(int idOrden, int variante, int cantidad, Integer idPedido) {
        for (Map<String, Object> d : jdbc.queryForList(
                "SELECT d.id_detalle_op, d.id_insumo, COALESCE(d.cantidad_real_usada, d.cantidad_estimada) AS uso, i.nombre "
                        + "FROM detalle_orden_produccion d JOIN insumo i ON i.id_insumo = d.id_insumo WHERE d.id_orden_produccion = ?", idOrden)) {
            BigDecimal uso = dec(d.get("uso"));
            int filas = jdbc.update("UPDATE insumo SET cantidad_disponible = cantidad_disponible - ? WHERE id_insumo = ? AND cantidad_disponible >= ?",
                    uso, ent(d.get("id_insumo")), uso);
            if (filas == 0) throw new ValidacionException("No hay insumo suficiente de «" + d.get("nombre") + "» (se necesitan " + uso.toPlainString() + ").");
            jdbc.update("UPDATE detalle_orden_produccion SET cantidad_real_usada = ? WHERE id_detalle_op = ? AND cantidad_real_usada IS NULL",
                    uso, ent(d.get("id_detalle_op")));
        }
        stock.agregar(variante, cantidad);
        if (idPedido != null) {
            Integer pendientes = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM orden_produccion WHERE id_pedido = ? AND estado_fase NOT IN ('Terminado', 'Cancelado')",
                    Integer.class, idPedido);
            if (Objects.equals(pendientes, 0)) {
                jdbc.update("UPDATE pedido SET estado = 'Listo' WHERE id_pedido = ? AND estado IN ('Recibido', 'En proceso')", idPedido);
            }
        }
    }
}
