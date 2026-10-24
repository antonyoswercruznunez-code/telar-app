package com.example.demo.clientes;

import com.example.demo.comun.Campo;
import com.example.demo.comun.Entidad;
import com.example.demo.comun.Lookups;
import com.example.demo.comun.ModuloCrud;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.example.demo.comun.Util.dec;
import static com.example.demo.comun.Util.ent;

/** Módulo Clientes y pedidos: pedido, detalle_pedido y medida_personalizada (cliente tiene pantallas propias). */
@Component
public class PedidosModulo implements ModuloCrud {
    private static final String M = "Clientes y pedidos";
    private final JdbcTemplate jdbc;

    public PedidosModulo(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public List<Entidad> entidades() {
        return List.of(
                Entidad.de(M, "pedido", "Pedidos", "Pedido", "pedido", "id_pedido",
                        Campo.fk("id_cliente", "Cliente", Lookups.CLIENTES).req(),
                        Campo.fk("id_usuario", "Registrado por", Lookups.USUARIOS).def("usuario"),
                        Campo.fechaHora("fecha_pedido", "Fecha del pedido").req().def("ahora"),
                        Campo.texto("descripcion", "Descripción"),
                        Campo.lista("estado", "Estado", "Recibido", "En proceso", "Listo", "Entregado", "Cancelado").req().def("Recibido"),
                        Campo.fecha("fecha_entrega", "Fecha de entrega"))
                        .hijo("Detalle", "detalle_pedido", "id_pedido")
                        .hijo("Producción", "orden_produccion", "id_pedido")
                        .hijo("Ventas", "venta", "id_pedido"),
                Entidad.de(M, "detalle_pedido", "Detalle de pedido", "Detalle de pedido", "detalle_pedido", "id_detalle",
                        Campo.fk("id_pedido", "Pedido", Lookups.PEDIDOS).req(),
                        Campo.fk("id_detalle_producto", "Producto (variante)", Lookups.VARIANTES).req(),
                        Campo.entero("cantidad", "Cantidad").req().def("1"),
                        Campo.decimal("subtotal", "Subtotal (vacío = precio × cantidad)"))
                        .hijo("Medidas", "medida_personalizada", "id_detalle_pedido"),
                Entidad.de(M, "medida_personalizada", "Medidas personalizadas", "Medida personalizada", "medida_personalizada", "id_medida",
                        Campo.fk("id_detalle_pedido", "Detalle de pedido", Lookups.DETALLES_PEDIDO).req(),
                        Campo.decimal("pecho", "Pecho (cm)"),
                        Campo.decimal("cintura", "Cintura (cm)"),
                        Campo.decimal("cadera", "Cadera (cm)"),
                        Campo.decimal("largo_total", "Largo total (cm)"),
                        Campo.area("observaciones_corte", "Observaciones de corte")));
    }

    @Override
    public void antes(Entidad e, Map<String, Object> v, Map<String, Object> anterior) {
        if ("detalle_pedido".equals(e.clave()) && v.get("subtotal") == null) {
            BigDecimal precio = jdbc.queryForObject(
                    "SELECT p.precio FROM detalle_producto dp JOIN producto p ON p.id_producto = dp.id_producto "
                            + "WHERE dp.id_detalle_producto = ?", BigDecimal.class, ent(v.get("id_detalle_producto")));
            v.put("subtotal", dec(precio).multiply(BigDecimal.valueOf(ent(v.get("cantidad")))));
        }
    }
}
