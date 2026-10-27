package com.example.demo.clientes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** RF-02: búsqueda de clientes por nombre, documento o teléfono. */
@ExtendWith(MockitoExtension.class)
class ClienteBusquedaTest {
    @Mock
    ClienteRepository repository;

    private ClienteService servicio() {
        return new ClienteService(repository, new ClienteValidador());
    }

    @Test
    void buscarDevuelveLosClientesQueEncuentraElRepositorio() {
        List<Map<String, Object>> esperado = List.of(Map.of("nombre", "Rosa"));
        when(repository.buscar("Rosa", false)).thenReturn(esperado);

        assertEquals(esperado, servicio().buscar("Rosa", false));
    }

    @Test
    void buscarSinResultadosDevuelveListaVacia() {
        when(repository.buscar("zzz", false)).thenReturn(List.of());

        assertTrue(servicio().buscar("zzz", false).isEmpty());
    }

    @Test
    void buscarPuedeIncluirClientesInactivos() {
        when(repository.buscar("", true)).thenReturn(List.of());

        servicio().buscar("", true);

        verify(repository).buscar("", true);
    }
}