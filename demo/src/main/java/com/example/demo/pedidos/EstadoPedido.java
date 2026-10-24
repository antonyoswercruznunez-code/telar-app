package com.example.demo.pedidos;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Estados de un pedido. El flujo normal es Recibido → En proceso → Listo → Entregado. */
public enum EstadoPedido {
    RECIBIDO("Recibido"),
    EN_PROCESO("En proceso"),
    LISTO("Listo"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }

    /** Siguiente estado del flujo normal; vacío si ya terminó o está cancelado. */
    public Optional<EstadoPedido> siguiente() {
        return switch (this) {
            case RECIBIDO -> Optional.of(EN_PROCESO);
            case EN_PROCESO -> Optional.of(LISTO);
            case LISTO -> Optional.of(ENTREGADO);
            case ENTREGADO, CANCELADO -> Optional.empty();
        };
    }

    public static Optional<EstadoPedido> de(String etiqueta) {
        return Arrays.stream(values()).filter(e -> e.etiqueta.equals(etiqueta)).findFirst();
    }

    public static List<String> etiquetas() {
        return Arrays.stream(values()).map(EstadoPedido::getEtiqueta).toList();
    }
}
