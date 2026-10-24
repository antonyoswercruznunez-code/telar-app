package com.example.demo.catalogo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {
    @Mock
    ProductoRepository repository;

    private ProductoService servicio() {
        return new ProductoService(repository, new ProductoValidador());
    }

    private ProductoForm producto(String nombre) {
        ProductoForm p = new ProductoForm();
        p.setNombre(nombre);
        p.setPrecio(new BigDecimal("50.00"));
        return p;
    }

    private VarianteForm variante() {
        VarianteForm v = new VarianteForm();
        v.setColor("Rojo");
        v.setTalla("M");
        return v;
    }

    @Test
    void productoNuevoConNombreRepetidoSeRechaza() {
        when(repository.existeNombre("Polo básico", 0)).thenReturn(true);
        assertTrue(servicio().validarNuevo(producto("Polo básico"), variante()).contains("Ya existe un producto con ese nombre."));
    }

    @Test
    void alEditarPuedeConservarSuPropioNombre() {
        when(repository.existeNombre("Polo básico", 4)).thenReturn(false);
        assertTrue(servicio().validarEdicion(4, producto("Polo básico")).isEmpty());
    }

    @Test
    void desactivarMarcaElProductoComoInactivo() {
        when(repository.cambiarEstado(2, false)).thenReturn(1);
        servicio().desactivar(2);
        verify(repository).cambiarEstado(2, false);
    }

    @Test
    void desactivarProductoInexistenteLanzaExcepcion() {
        when(repository.cambiarEstado(99, false)).thenReturn(0);
        assertThrows(NoSuchElementException.class, () -> servicio().desactivar(99));
    }
}
