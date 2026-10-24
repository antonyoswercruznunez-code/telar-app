package com.example.demo.compras;

import com.example.demo.comun.Campo;
import com.example.demo.comun.Entidad;
import com.example.demo.comun.Lookups;
import com.example.demo.comun.ModuloCrud;
import com.example.demo.comun.ValidacionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.example.demo.comun.Util.dec;
import static com.example.demo.comun.Util.ent;

/**
 * Módulo Compras e insumos.
 * Cada línea de compra suma su cantidad al insumo (multiplicada por el factor de conversion_unidad si existe)
 * y el total de la compra se calcula con el detalle.
 */
@Component
public class ComprasModulo implements ModuloCrud {
    private static final String M = "Compras e insumos";
    private final JdbcTemplate jdbc;

    public ComprasModulo(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public List<Entidad> entidades() {
        return List.of(
                Entidad.de(M, "proveedor", "Proveedores", "Proveedor", "proveedor", "id_proveedor",
                        Campo.texto("razon_social", "Razón social").req(),
                        Campo.texto("ruc_rfc", "RUC / RFC").req(),
                        Campo.texto("telefono", "Teléfono"),
                        Campo.texto("correo", "Correo"),
                        Campo.texto("direccion", "Dirección"))
                        .hijo("Compras", "compra", "id_proveedor"),
                Entidad.de(M, "insumo", "Insumos", "Insumo", "insumo", "id_insumo",
                        Campo.texto("nombre", "Nombre").req(),
                        Campo.lista("unidad_medida", "Unidad de medida", "Metro", "Kilogramo", "Gramo", "Unidad", "Rollo", "Cono", "Litro", "Docena").req(),
                        Campo.decimal("cantidad_disponible", "Cantidad disponible").req().def("0"),
                        Campo.decimal("cantidad_minima", "Cantidad mínima").req().def("0"))
                        .hijo("Conversiones", "conversion_unidad", "id_insumo")
                        .hijo("Fichas", "ficha_tecnica", "id_insumo"),
                Entidad.de(M, "compra", "Compras", "Compra", "compra", "id_compra",
                        Campo.fk("id_proveedor", "Proveedor", Lookups.PROVEEDORES).req(),
                        Campo.fk("id_usuario", "Registrada por", Lookups.USUARIOS).req().def("usuario"),
                        Campo.fechaHora("fecha_compra", "Fecha").req().def("ahora"),
                        Campo.texto("numero_comprobante", "N.º de comprobante"),
                        Campo.decimal("total", "Total (si hay detalle se calcula solo)").req().def("0"))
                        .hijo("Detalle", "detalle_compra", "id_compra"),
                Entidad.de(M, "detalle_compra", "Detalle de compra", "Detalle de compra", "detalle_compra", "id_detalle_compra",
                        Campo.fk("id_compra", "Compra", Lookups.COMPRAS).req(),
                        Campo.fk("id_insumo", "Insumo", Lookups.INSUMOS).req(),
                        Campo.entero("cantidad", "Cantidad comprada").req().def("1"),
                        Campo.decimal("precio_compra_unitario", "Precio unitario").req()),
                Entidad.de(M, "conversion_unidad", "Conversión de unidades", "Conversión", "conversion_unidad", "id_conversion",
                        Campo.fk("id_insumo", "Insumo", Lookups.INSUMOS).req(),
                        Campo.texto("unidad_compra", "Unidad de compra (ej. rollo)").req(),
                        Campo.texto("unidad_consumo", "Unidad de consumo (ej. metro)").req(),
                        Campo.decimal("factor_conversion", "Factor (1 unidad de compra = ? de consumo)").req().def("1")));
    }

    @Override
    public void antes(Entidad e, Map<String, Object> v, Map<String, Object> anterior) {
        if ("detalle_compra".equals(e.clave()) && ent(v.get("cantidad")) <= 0) {
            throw new ValidacionException("La cantidad debe ser mayor que cero.");
        }
        if ("conversion_unidad".equals(e.clave()) && dec(v.get("factor_conversion")).signum() <= 0) {
            throw new ValidacionException("El factor de conversión debe ser mayor que cero.");
        }
    }

    @Override
    public void despues(Entidad e, int id, Map<String, Object> v, Map<String, Object> anterior) {
        switch (e.clave()) {
            case "compra" -> recalcular(id);
            case "detalle_compra" -> {
                if (anterior != null) ajustarInsumo(ent(anterior.get("id_insumo")), ent(anterior.get("cantidad")), -1);
                ajustarInsumo(ent(v.get("id_insumo")), ent(v.get("cantidad")), 1);
                recalcular(ent(v.get("id_compra")));
                if (anterior != null && ent(anterior.get("id_compra")) != ent(v.get("id_compra"))) recalcular(ent(anterior.get("id_compra")));
            }
            default -> { }
        }
    }

    @Override
    public void antesEliminar(Entidad e, Map<String, Object> anterior) {
        if ("compra".equals(e.clave())) {
            for (Map<String, Object> d : jdbc.queryForList(
                    "SELECT id_insumo, cantidad FROM detalle_compra WHERE id_compra = ?", ent(anterior.get("id_compra")))) {
                ajustarInsumo(ent(d.get("id_insumo")), ent(d.get("cantidad")), -1);
            }
        }
    }

    @Override
    public void despuesEliminar(Entidad e, Map<String, Object> anterior) {
        if ("detalle_compra".equals(e.clave())) {
            ajustarInsumo(ent(anterior.get("id_insumo")), ent(anterior.get("cantidad")), -1);
            recalcular(ent(anterior.get("id_compra")));
        }
    }

    /** Suma (signo 1) o resta (signo −1) al insumo; usa el primer factor de conversion_unidad del insumo, o 1 si no hay. */
    private void ajustarInsumo(int idInsumo, int cantidad, int signo) {
        List<BigDecimal> factores = jdbc.queryForList(
                "SELECT factor_conversion FROM conversion_unidad WHERE id_insumo = ? ORDER BY id_conversion LIMIT 1", BigDecimal.class, idInsumo);
        BigDecimal factor = factores.isEmpty() ? BigDecimal.ONE : factores.get(0);
        BigDecimal delta = factor.multiply(BigDecimal.valueOf((long) cantidad * signo));
        jdbc.update("UPDATE insumo SET cantidad_disponible = GREATEST(cantidad_disponible + ?, 0) WHERE id_insumo = ?", delta, idInsumo);
    }

    private void recalcular(int idCompra) {
        Map<String, Object> det = jdbc.queryForMap(
                "SELECT COUNT(*) AS n, COALESCE(SUM(cantidad * precio_compra_unitario), 0) AS s FROM detalle_compra WHERE id_compra = ?", idCompra);
        if (ent(det.get("n")) > 0) jdbc.update("UPDATE compra SET total = ? WHERE id_compra = ?", dec(det.get("s")), idCompra);
    }
}
