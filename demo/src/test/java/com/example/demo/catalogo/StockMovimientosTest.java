package com.example.demo.catalogo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Movimientos de ingreso de stock y consulta de existencias (apoya RF-17 a RF-19). */
@ExtendWith(MockitoExtension.class)
class StockMovimientosTest {
    @Mock
    JdbcTemplate jdbc;

    @Test
    void disponibleDevuelveCeroCuandoNoHayRegistrosDeStock() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(9))).thenReturn(null);

        assertEquals(0, new StockService(jdbc).disponible(9));
    }

    @Test
    void agregarSumaALaFilaExistenteSinCrearOtra() {
        when(jdbc.update(contains("UPDATE stock SET cantidad_disponible = cantidad_disponible + ?"), eq(5), eq(7)))
                .thenReturn(1);

        new StockService(jdbc).agregar(7, 5);

        verify(jdbc, never()).update(contains("INSERT INTO stock"), eq(7), eq(5));
    }

    @Test
    void agregarCreaLaFilaDeStockSiLaVarianteNoTeniaNinguna() {
        when(jdbc.update(contains("UPDATE stock SET cantidad_disponible = cantidad_disponible + ?"), eq(5), eq(7)))
                .thenReturn(0);

        new StockService(jdbc).agregar(7, 5);

        verify(jdbc).update(contains("INSERT INTO stock"), eq(7), eq(5));
    }

    @Test
    void agregarCantidadCeroONegativaNoTocaLaBaseDeDatos() {
        new StockService(jdbc).agregar(7, 0);
        new StockService(jdbc).agregar(7, -3);

        verifyNoInteractions(jdbc);
    }

    @Test
    void agregarConVarianteInvalidaSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> new StockService(jdbc).agregar(0, 5));
    }
}