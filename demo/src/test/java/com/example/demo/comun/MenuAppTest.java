package com.example.demo.comun;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class MenuAppTest {
    @Mock
    Entidades entidades;

    private MenuApp menu;

    @BeforeEach
    void preparar() {
        lenient().when(entidades.titulo(anyString())).thenAnswer(inv -> inv.getArgument(0));
        menu = new MenuApp(entidades);
    }

    private List<String> titulos(Set<String> roles) {
        return menu.grupos(roles).stream().flatMap(g -> g.items().stream()).map(MenuApp.Item::titulo).toList();
    }

    @Test
    void administradorVeUsuariosYTodosLosGrupos() {
        assertTrue(titulos(Set.of("ADMIN")).contains("Usuarios"));
        assertTrue(menu.grupos(Set.of("ADMIN")).size() == 8);
    }

    @Test
    void vendedorVeLoOperativoPeroNoUsuariosProduccionNiCompras() {
        List<String> t = titulos(Set.of("VENDEDOR"));
        assertTrue(t.contains("Clientes"));
        assertTrue(t.contains("venta"));
        assertTrue(t.contains("Inventario"));
        assertFalse(t.contains("Usuarios"));
        assertFalse(t.contains("orden_produccion"));
        assertFalse(t.contains("compra"));
        assertTrue(t.contains("Seguimiento de pedidos"));
        assertTrue(t.contains("Reportes (Excel y PDF)"));
        assertTrue(menu.grupos(Set.of("VENDEDOR")).size() == 5);
        assertTrue(t.contains("Dashboard"));
    }

    @Test
    void sinRolesElMenuQuedaVacio() {
        assertTrue(menu.grupos(Set.of()).isEmpty());
    }
}
