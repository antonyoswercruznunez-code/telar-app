package com.example.demo.clientes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {
    @Mock
    ClienteRepository repository;

    private ClienteService servicio() {
        return new ClienteService(repository, new ClienteValidador());
    }

    private ClienteForm cliente(String documento) {
        ClienteForm c = new ClienteForm();
        c.setNombre("Rosa");
        c.setTipoDocumento("DNI");
        c.setNumeroDocumento(documento);
        return c;
    }

    @Test
    void clienteNuevoConDocumentoRepetidoSeRechaza() {
        when(repository.existeDocumento("45871236")).thenReturn(true);
        assertTrue(servicio().validarNuevo(cliente("45871236")).contains("Ya existe un cliente con ese número de documento."));
    }

    @Test
    void alEditarPuedeConservarSuPropioDocumento() {
        when(repository.existeDocumentoDeOtro("45871236", 7)).thenReturn(false);
        assertTrue(servicio().validarEdicion(7, cliente("45871236")).isEmpty());
    }

    @Test
    void alEditarNoPuedeUsarElDocumentoDeOtroCliente() {
        when(repository.existeDocumentoDeOtro("71234589", 7)).thenReturn(true);
        assertTrue(servicio().validarEdicion(7, cliente("71234589")).contains("Ya existe otro cliente con ese número de documento."));
    }

    @Test
    void desactivarMarcaAlClienteComoInactivo() {
        when(repository.cambiarEstado(3, false)).thenReturn(1);
        servicio().desactivar(3);
        verify(repository).cambiarEstado(3, false);
    }

    @Test
    void desactivarClienteInexistenteLanzaExcepcion() {
        when(repository.cambiarEstado(99, false)).thenReturn(0);
        assertThrows(NoSuchElementException.class, () -> servicio().desactivar(99));
    }

    @Test
    void historialSumaTotalYSaldoPendiente() {
        when(repository.buscarPorId(1)).thenReturn(Optional.of(cliente("45871236")));
        when(repository.pedidosDe(1)).thenReturn(List.of());
        when(repository.ventasDe(1)).thenReturn(List.of(
                Map.<String, Object>of("total", new BigDecimal("100.50"), "saldo", new BigDecimal("20.00")),
                Map.<String, Object>of("total", new BigDecimal("50.00"), "saldo", BigDecimal.ZERO)));

        HistorialCliente h = servicio().historial(1).orElseThrow();

        assertEquals(new BigDecimal("150.50"), h.totalComprado());
        assertEquals(new BigDecimal("20.00"), h.saldoPendiente());
    }

    @Test
    void historialDeClienteInexistenteEstaVacio() {
        when(repository.buscarPorId(5)).thenReturn(Optional.empty());
        assertTrue(servicio().historial(5).isEmpty());
    }
}
