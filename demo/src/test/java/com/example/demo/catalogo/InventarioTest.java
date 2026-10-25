package com.example.demo.catalogo;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import org.springframework.ui.ExtendedModelMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventarioTest {
    @Test
    void servicioConservaArticulosYTotalesDelDao() {
        var dao = mock(InventarioDao.class);
        var esperado = new ResumenInventario(List.of(Map.of("nombre", "Camisa")), 1, 2, 1);
        when(dao.consultar()).thenReturn(esperado);
        assertEquals(esperado, new InventarioService(dao).consultar());
        verify(dao).consultar();
    }

    @Test
    void inventarioVacioConservaTotalesEnCero() {
        var dao = mock(InventarioDao.class);
        when(dao.consultar()).thenReturn(new ResumenInventario(List.of(), 0, 0, 0));
        var resultado = new InventarioService(dao).consultar();
        assertTrue(resultado.articulos().isEmpty());
        assertEquals(0, resultado.totalProductos());
        assertEquals(0, resultado.totalVariantes());
        assertEquals(0, resultado.alertasStock());
    }

    @Test
    void controladorPublicaLosAtributosDeLaVistaExistente() {
        var servicio = mock(InventarioService.class);
        var resumen = new ResumenInventario(List.of(Map.of("nombre", "Camisa")), 1, 2, 1);
        when(servicio.consultar()).thenReturn(resumen);
        var modelo = new ExtendedModelMap();
        assertEquals("catalogo/inventario", new InventarioController(servicio).inventario(modelo));
        assertEquals(resumen.articulos(), modelo.get("articulos"));
        assertEquals(1, modelo.get("totalProductos"));
        assertEquals(2, modelo.get("totalVariantes"));
        assertEquals(1, modelo.get("alertasStock"));
        verify(servicio).consultar();
    }
}
