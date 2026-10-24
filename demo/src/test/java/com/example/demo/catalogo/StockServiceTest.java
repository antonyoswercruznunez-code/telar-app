package com.example.demo.catalogo;

import com.example.demo.comun.ValidacionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {
    @Mock
    JdbcTemplate jdbc;

    @Test
    void descontarCantidadCeroNoTocaLaBaseDeDatos() {
        new StockService(jdbc).descontar(5, 0);
        verifyNoInteractions(jdbc);
    }

    @Test
    void descontarConStockInsuficienteLanzaValidacionException() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(5))).thenReturn(2);
        when(jdbc.queryForList(anyString(), eq(String.class), eq(5))).thenReturn(List.of("Camisa · Rojo · M"));

        ValidacionException ex = assertThrows(ValidacionException.class, () -> new StockService(jdbc).descontar(5, 10));

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    @Test
    void descontarConStockSuficienteActualizaLaFilaDeStock() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(7))).thenReturn(10);
        when(jdbc.queryForList(anyString(), eq(7))).thenReturn(List.of(Map.<String, Object>of("id_stock", 100, "cantidad_disponible", 10)));

        new StockService(jdbc).descontar(7, 3);

        verify(jdbc).update(contains("UPDATE stock SET cantidad_disponible = cantidad_disponible - ?"), eq(3), eq(100));
    }

    @Test
    void varianteInvalidaSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> new StockService(jdbc).descontar(0, 1));
    }
}
