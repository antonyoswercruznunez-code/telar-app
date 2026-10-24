package com.example.demo.pedidos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoSeguimientoServiceTest {
    @Mock
    PedidoSeguimientoRepository repository;

    private PedidoSeguimientoService servicio() {
        return new PedidoSeguimientoService(repository);
    }

    @Test
    void avanzarPasaAlSiguienteEstadoDelFlujo() {
        when(repository.estadoDe(1)).thenReturn(Optional.of("Recibido"));
        assertEquals("En proceso", servicio().avanzar(1));
        verify(repository).cambiarEstado(1, "En proceso");
    }

    @Test
    void avanzarUnPedidoEntregadoLanzaExcepcion() {
        when(repository.estadoDe(2)).thenReturn(Optional.of("Entregado"));
        assertThrows(IllegalStateException.class, () -> servicio().avanzar(2));
    }

    @Test
    void avanzarPedidoInexistenteLanzaExcepcion() {
        when(repository.estadoDe(9)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> servicio().avanzar(9));
    }

    @Test
    void cambiarAUnEstadoInvalidoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> servicio().cambiarEstado(1, "Volando"));
    }

    @Test
    void cambiarEstadoValidoLoGuarda() {
        when(repository.cambiarEstado(3, "Cancelado")).thenReturn(1);
        servicio().cambiarEstado(3, "Cancelado");
        verify(repository).cambiarEstado(3, "Cancelado");
    }
}
