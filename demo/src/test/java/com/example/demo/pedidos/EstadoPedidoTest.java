package com.example.demo.pedidos;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoPedidoTest {
    @Test
    void elFlujoNormalEsRecibidoEnProcesoListoEntregado() {
        assertEquals(Optional.of(EstadoPedido.EN_PROCESO), EstadoPedido.RECIBIDO.siguiente());
        assertEquals(Optional.of(EstadoPedido.LISTO), EstadoPedido.EN_PROCESO.siguiente());
        assertEquals(Optional.of(EstadoPedido.ENTREGADO), EstadoPedido.LISTO.siguiente());
    }

    @Test
    void entregadoYCanceladoNoAvanzan() {
        assertTrue(EstadoPedido.ENTREGADO.siguiente().isEmpty());
        assertTrue(EstadoPedido.CANCELADO.siguiente().isEmpty());
    }

    @Test
    void seReconocenPorSuEtiqueta() {
        assertEquals(Optional.of(EstadoPedido.EN_PROCESO), EstadoPedido.de("En proceso"));
        assertTrue(EstadoPedido.de("Pendiente").isEmpty());
    }
}
