package com.example.demo.ventas;

import com.example.demo.catalogo.StockService;
import com.example.demo.comun.Campo;
import com.example.demo.comun.Entidad;
import com.example.demo.comun.Lookups;
import com.example.demo.comun.ModuloCrud;
import com.example.demo.comun.ValidacionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.example.demo.comun.Util.ahora;
import static com.example.demo.comun.Util.dec;
import static com.example.demo.comun.Util.ent;
import static com.example.demo.comun.Util.str;

/**
 * Módulo Ventas, pagos y caja.
 * Reglas: el detalle de venta descuenta stock y recalcula el total; los pagos recalculan saldo y estado de pago;
 * el comprobante se numera solo; el cierre de caja y el reporte calculan sus totales.
 */
@Component
public class VentasModulo implements ModuloCrud {
    private static final String M = "Ventas, pagos y caja";
    private final JdbcTemplate jdbc;
    private final StockService stock;

    public VentasModulo(JdbcTemplate jdbc, StockService stock) {
        this.jdbc = jdbc;
        this.stock = stock;
    }

    @Override
    public List<Entidad> entidades() {
        return List.of(
                Entidad.de(M, "venta", "Ventas", "Venta", "venta", "id_venta",
                        Campo.fk("id_cliente", "Cliente", Lookups.CLIENTES).req(),
                        Campo.fk("id_pedido", "Pedido", Lookups.PEDIDOS),
                        Campo.fk("id_usuario", "Vendedor", Lookups.USUARIOS).req().def("usuario"),
                        Campo.lista("tipo_venta", "Tipo de venta", "Minorista", "Mayorista", "Por pedido").req().def("Minorista"),
                        Campo.fechaHora("fecha_venta", "Fecha").req().def("ahora"),
                        Campo.decimal("total", "Total (si hay detalle se calcula solo)").req().def("0"),
                        Campo.decimal("monto_adelanto", "Adelanto").req().def("0"),
                        Campo.decimal("saldo_pendiente", "Saldo pendiente").oculto(),
                        Campo.lista("estado_pago", "Estado de pago", "Pagado", "Pendiente", "Adelanto").req().def("Pagado"))
                        .hijo("Detalle", "detalle_venta", "id_venta")
                        .hijo("Pagos", "pago_venta", "id_venta")
                        .hijo("Comprobantes", "comprobante", "id_venta"),
                Entidad.de(M, "detalle_venta", "Detalle de venta", "Detalle de venta", "detalle_venta", "id_detalle_venta",
                        Campo.fk("id_venta", "Venta", Lookups.VENTAS).req(),
                        Campo.fk("id_detalle_producto", "Producto (variante)", Lookups.VARIANTES).req(),
                        Campo.entero("cantidad", "Cantidad").req().def("1"),
                        Campo.decimal("precio_unitario", "Precio unitario (vacío = precio del catálogo)")),
                Entidad.de(M, "metodo_pago", "Métodos de pago", "Método de pago", "metodo_pago", "id_metodo_pago",
                        Campo.texto("nombre", "Nombre").req())
                        .hijo("Pagos", "pago_venta", "id_metodo_pago"),
                Entidad.de(M, "pago_venta", "Pagos", "Pago", "pago_venta", "id_pago",
                        Campo.fk("id_venta", "Venta", Lookups.VENTAS).req(),
                        Campo.fk("id_metodo_pago", "Método de pago", Lookups.METODOS_PAGO).req(),
                        Campo.decimal("monto", "Monto").req(),
                        Campo.texto("referencia", "Referencia / operación"),
                        Campo.fechaHora("fecha_pago", "Fecha de pago").req().def("ahora")),
                Entidad.de(M, "comprobante", "Comprobantes", "Comprobante", "comprobante", "id_comprobante",
                        Campo.fk("id_venta", "Venta", Lookups.VENTAS).req(),
                        Campo.lista("tipo_comprobante", "Tipo", "Boleta", "Factura", "Nota de venta").req().def("Boleta"),
                        Campo.texto("serie", "Serie").req().def("B001"),
                        Campo.entero("numero", "Número (vacío = correlativo automático)"),
                        Campo.fechaHora("fecha_emision", "Fecha de emisión").oculto(),
                        Campo.lista("estado", "Estado", "Emitido", "Anulado").req().def("Emitido")),
                Entidad.de(M, "cierre_caja", "Cierre de caja", "Cierre de caja", "cierre_caja", "id_cierre",
                        Campo.fk("id_usuario", "Cajero", Lookups.USUARIOS).req().def("usuario"),
                        Campo.fechaHora("fecha_apertura", "Apertura").req().def("ahora"),
                        Campo.fechaHora("fecha_cierre", "Cierre (vacío = ahora al cerrar)"),
                        Campo.decimal("monto_inicial", "Monto inicial").req().def("0"),
                        Campo.decimal("monto_final_efectivo", "Efectivo contado al cierre"),
                        Campo.decimal("total_ventas_calculado", "Total de ventas calculado").oculto(),
                        Campo.lista("estado", "Estado", "Abierta", "Cerrada").req().def("Abierta")),
                Entidad.de(M, "reporte", "Reportes", "Reporte", "reporte", "id_reporte",
                        Campo.fk("id_usuario", "Generado por", Lookups.USUARIOS).req().def("usuario"),
                        Campo.lista("tipo", "Tipo", "Diario", "Semanal", "Mensual", "Personalizado").req().def("Diario"),
                        Campo.fecha("fecha_inicio", "Desde").req().def("hoy"),
                        Campo.fecha("fecha_fin", "Hasta").req().def("hoy"),
                        Campo.decimal("total_vendido", "Total vendido").oculto(),
                        Campo.fk("id_producto_mas_vendido", "Producto más vendido", Lookups.PRODUCTOS).oculto(),
                        Campo.fk("id_producto_faltante", "Producto con faltante", Lookups.PRODUCTOS).oculto(),
                        Campo.fechaHora("fecha_generacion", "Generado").oculto()));
    }

    // ---------- Antes de guardar ----------

    @Override
    public void antes(Entidad e, Map<String, Object> v, Map<String, Object> anterior) {
        switch (e.clave()) {
            case "venta" -> {
                BigDecimal total = dec(v.get("total"));
                BigDecimal adelanto = dec(v.get("monto_adelanto"));
                if (adelanto.compareTo(total) > 0) throw new ValidacionException("El adelanto no puede ser mayor que el total.");
                boolean pagado = "Pagado".equals(str(v.get("estado_pago")));
                v.put("saldo_pendiente", pagado ? BigDecimal.ZERO : total.subtract(adelanto));
            }
            case "detalle_venta" -> {
                if (ent(v.get("cantidad")) <= 0) throw new ValidacionException("La cantidad debe ser mayor que cero.");
                if (v.get("precio_unitario") == null) v.put("precio_unitario", precioSugerido(v));
            }
            case "pago_venta" -> {
                if (dec(v.get("monto")).signum() <= 0) throw new ValidacionException("El monto del pago debe ser mayor que cero.");
            }
            case "comprobante" -> numerarComprobante(v, anterior);
            case "cierre_caja" -> prepararCierre(v, anterior);
            case "reporte" -> calcularReporte(v);
            default -> { }
        }
    }

    private BigDecimal precioSugerido(Map<String, Object> v) {
        Map<String, Object> p = jdbc.queryForMap(
                "SELECT p.precio, p.precio_mayorista, p.cantidad_minima_mayorista FROM detalle_producto dp "
                        + "JOIN producto p ON p.id_producto = dp.id_producto WHERE dp.id_detalle_producto = ?",
                ent(v.get("id_detalle_producto")));
        List<String> tipo = jdbc.queryForList("SELECT tipo_venta FROM venta WHERE id_venta = ?", String.class, ent(v.get("id_venta")));
        boolean mayorista = !tipo.isEmpty() && "Mayorista".equals(tipo.get(0)) && p.get("precio_mayorista") != null
                && ent(v.get("cantidad")) >= (p.get("cantidad_minima_mayorista") == null ? 12 : ent(p.get("cantidad_minima_mayorista")));
        return mayorista ? dec(p.get("precio_mayorista")) : dec(p.get("precio"));
    }

    private void numerarComprobante(Map<String, Object> v, Map<String, Object> anterior) {
        String tipo = str(v.get("tipo_comprobante"));
        String serie = str(v.get("serie"));
        int idActual = anterior == null ? 0 : ent(anterior.get("id_comprobante"));
        if (v.get("numero") == null) {
            if (anterior != null && tipo.equals(str(anterior.get("tipo_comprobante"))) && serie.equals(str(anterior.get("serie")))) {
                v.put("numero", anterior.get("numero"));
            } else {
                Integer siguiente = jdbc.queryForObject(
                        "SELECT COALESCE(MAX(numero), 0) + 1 FROM comprobante WHERE tipo_comprobante = ? AND serie = ?",
                        Integer.class, tipo, serie);
                v.put("numero", siguiente);
            }
        } else {
            Integer repetidos = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM comprobante WHERE tipo_comprobante = ? AND serie = ? AND numero = ? AND id_comprobante <> ?",
                    Integer.class, tipo, serie, ent(v.get("numero")), idActual);
            if (repetidos != null && repetidos > 0) throw new ValidacionException("Ya existe un comprobante con esa serie y número.");
        }
    }

    private void prepararCierre(Map<String, Object> v, Map<String, Object> anterior) {
        int usuario = ent(v.get("id_usuario"));
        String estado = str(v.get("estado"));
        int idActual = anterior == null ? 0 : ent(anterior.get("id_cierre"));
        if ("Abierta".equals(estado)) {
            Integer abiertas = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM cierre_caja WHERE id_usuario = ? AND estado = 'Abierta' AND id_cierre <> ?",
                    Integer.class, usuario, idActual);
            if (abiertas != null && abiertas > 0) throw new ValidacionException("Este cajero ya tiene una caja abierta. Ciérrala antes de abrir otra.");
            v.put("fecha_cierre", null);
            v.put("total_ventas_calculado", BigDecimal.ZERO);
            return;
        }
        if (v.get("monto_final_efectivo") == null) throw new ValidacionException("Indica el efectivo contado al cierre.");
        Timestamp apertura = (Timestamp) v.get("fecha_apertura");
        Timestamp cierre = v.get("fecha_cierre") == null ? ahora() : (Timestamp) v.get("fecha_cierre");
        if (cierre.before(apertura)) throw new ValidacionException("El cierre no puede ser anterior a la apertura.");
        BigDecimal cobrado = jdbc.queryForObject(
                "SELECT COALESCE(SUM(total - saldo_pendiente), 0) FROM venta WHERE id_usuario = ? AND fecha_venta BETWEEN ? AND ?",
                BigDecimal.class, usuario, apertura, cierre);
        v.put("fecha_cierre", cierre);
        v.put("total_ventas_calculado", dec(cobrado));
    }

    private void calcularReporte(Map<String, Object> v) {
        var inicio = ((Date) v.get("fecha_inicio")).toLocalDate();
        var fin = ((Date) v.get("fecha_fin")).toLocalDate();
        if (fin.isBefore(inicio)) throw new ValidacionException("La fecha final no puede ser anterior a la inicial.");
        Timestamp desde = Timestamp.valueOf(inicio.atStartOfDay());
        Timestamp hasta = Timestamp.valueOf(fin.plusDays(1).atStartOfDay());
        BigDecimal total = jdbc.queryForObject(
                "SELECT COALESCE(SUM(total), 0) FROM venta WHERE fecha_venta >= ? AND fecha_venta < ?", BigDecimal.class, desde, hasta);
        List<Integer> masVendido = jdbc.queryForList(
                "SELECT dp.id_producto FROM detalle_venta dv JOIN venta ve ON ve.id_venta = dv.id_venta "
                        + "JOIN detalle_producto dp ON dp.id_detalle_producto = dv.id_detalle_producto "
                        + "WHERE ve.fecha_venta >= ? AND ve.fecha_venta < ? GROUP BY dp.id_producto "
                        + "ORDER BY SUM(dv.cantidad) DESC LIMIT 1", Integer.class, desde, hasta);
        List<Integer> faltante = jdbc.queryForList(
                "SELECT dp.id_producto FROM stock s JOIN detalle_producto dp ON dp.id_detalle_producto = s.id_detalle_producto "
                        + "GROUP BY dp.id_producto HAVING SUM(s.cantidad_disponible) <= SUM(s.cantidad_minima) "
                        + "ORDER BY SUM(s.cantidad_disponible) - SUM(s.cantidad_minima) LIMIT 1", Integer.class);
        v.put("total_vendido", dec(total));
        v.put("id_producto_mas_vendido", masVendido.isEmpty() ? null : masVendido.get(0));
        v.put("id_producto_faltante", faltante.isEmpty() ? null : faltante.get(0));
        v.put("fecha_generacion", Timestamp.valueOf(LocalDateTime.now()));
    }

    // ---------- Después de guardar ----------

    @Override
    public void despues(Entidad e, int id, Map<String, Object> v, Map<String, Object> anterior) {
        switch (e.clave()) {
            case "venta" -> recalcular(id);
            case "detalle_venta" -> {
                if (anterior != null) stock.agregar(ent(anterior.get("id_detalle_producto")), ent(anterior.get("cantidad")));
                stock.descontar(ent(v.get("id_detalle_producto")), ent(v.get("cantidad")));
                recalcular(ent(v.get("id_venta")));
                if (anterior != null && ent(anterior.get("id_venta")) != ent(v.get("id_venta"))) recalcular(ent(anterior.get("id_venta")));
            }
            case "pago_venta" -> {
                recalcular(ent(v.get("id_venta")));
                if (anterior != null && ent(anterior.get("id_venta")) != ent(v.get("id_venta"))) recalcular(ent(anterior.get("id_venta")));
            }
            default -> { }
        }
    }

    @Override
    public void antesEliminar(Entidad e, Map<String, Object> anterior) {
        if ("venta".equals(e.clave())) {
            for (Map<String, Object> d : jdbc.queryForList(
                    "SELECT id_detalle_producto, cantidad FROM detalle_venta WHERE id_venta = ?", ent(anterior.get("id_venta")))) {
                stock.agregar(ent(d.get("id_detalle_producto")), ent(d.get("cantidad")));
            }
        }
    }

    @Override
    public void despuesEliminar(Entidad e, Map<String, Object> anterior) {
        switch (e.clave()) {
            case "detalle_venta" -> {
                stock.agregar(ent(anterior.get("id_detalle_producto")), ent(anterior.get("cantidad")));
                recalcular(ent(anterior.get("id_venta")));
            }
            case "pago_venta" -> recalcular(ent(anterior.get("id_venta")));
            default -> { }
        }
    }

    /**
     * Total = suma del detalle (si existe). Si hay pagos registrados, saldo = total − pagos y el estado se deduce;
     * si no hay pagos, se respeta el estado elegido (Pagado ⇒ saldo 0; otro ⇒ total − adelanto).
     */
    private void recalcular(int idVenta) {
        Map<String, Object> venta = jdbc.queryForMap("SELECT total, monto_adelanto, estado_pago FROM venta WHERE id_venta = ?", idVenta);
        BigDecimal total = dec(venta.get("total"));
        Map<String, Object> det = jdbc.queryForMap(
                "SELECT COUNT(*) AS n, COALESCE(SUM(cantidad * precio_unitario), 0) AS s FROM detalle_venta WHERE id_venta = ?", idVenta);
        if (ent(det.get("n")) > 0) total = dec(det.get("s"));
        Map<String, Object> pagos = jdbc.queryForMap(
                "SELECT COUNT(*) AS n, COALESCE(SUM(monto), 0) AS s FROM pago_venta WHERE id_venta = ?", idVenta);
        BigDecimal adelanto = dec(venta.get("monto_adelanto"));
        String estado = str(venta.get("estado_pago"));
        BigDecimal saldo;
        if (ent(pagos.get("n")) > 0) {
            BigDecimal pagado = dec(pagos.get("s"));
            saldo = total.subtract(pagado).max(BigDecimal.ZERO);
            adelanto = pagado;
            estado = saldo.signum() == 0 ? "Pagado" : (pagado.signum() > 0 ? "Adelanto" : "Pendiente");
        } else {
            saldo = "Pagado".equals(estado) ? BigDecimal.ZERO : total.subtract(adelanto).max(BigDecimal.ZERO);
        }
        jdbc.update("UPDATE venta SET total = ?, monto_adelanto = ?, saldo_pendiente = ?, estado_pago = ? WHERE id_venta = ?",
                total, adelanto, saldo, estado, idVenta);
    }
}
