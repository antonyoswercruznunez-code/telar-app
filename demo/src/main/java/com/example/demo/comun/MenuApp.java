package com.example.demo.comun;

import com.example.demo.seguridad.Permisos;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Menú lateral agrupado por módulos (ver fragments/menu.html). Cada opción indica qué roles la ven. */
@Component("menuApp")
public class MenuApp {
    public record Item(String titulo, String url, List<String> roles) { }

    public record Grupo(String titulo, List<Item> items) { }

    private final Entidades entidades;

    public MenuApp(Entidades entidades) { this.entidades = entidades; }

    private Item g(String clave, List<String> roles) { return new Item(entidades.titulo(clave), "/gestion/" + clave, roles); }

    private static Item i(String titulo, String url, List<String> roles) { return new Item(titulo, url, roles); }

    /** Grupos visibles para el usuario conectado. */
    public List<Grupo> grupos() {
        Set<String> roles = new HashSet<>();
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null) {
            for (GrantedAuthority autoridad : a.getAuthorities()) {
                roles.add(StringUtils.removeStart(autoridad.getAuthority(), "ROLE_"));
            }
        }
        return grupos(roles);
    }

    public List<Grupo> grupos(Set<String> rolesUsuario) {
        List<Grupo> todos = List.of(
                new Grupo("Inicio", List.of(i("Dashboard", "/dashboard", Permisos.TODOS))),
                new Grupo("Seguridad y usuarios", List.of(i("Usuarios", "/usuarios/nuevo", Permisos.ADMIN))),
                new Grupo("Clientes y pedidos", List.of(i("Clientes", "/clientes", Permisos.TODOS), i("Seguimiento de pedidos", "/pedidos/seguimiento", Permisos.TODOS),
                        g("pedido", Permisos.TODOS), g("detalle_pedido", Permisos.TODOS), g("medida_personalizada", Permisos.TODOS))),
                new Grupo("Catálogo e inventario", List.of(i("Inventario", "/inventario", Permisos.TODOS),
                        i("Productos", "/productos", Permisos.TODOS), g("stock", Permisos.ADMIN))),
                new Grupo("Ventas, pagos y caja", List.of(g("venta", Permisos.TODOS), g("detalle_venta", Permisos.TODOS),
                        g("metodo_pago", Permisos.TODOS), g("pago_venta", Permisos.TODOS), g("comprobante", Permisos.TODOS),
                        g("cierre_caja", Permisos.TODOS), g("reporte", Permisos.TODOS))),
                new Grupo("Producción", List.of(g("orden_produccion", Permisos.ADMIN),
                        g("detalle_orden_produccion", Permisos.ADMIN), g("ficha_tecnica", Permisos.ADMIN))),
                new Grupo("Compras e insumos", List.of(g("proveedor", Permisos.ADMIN), g("insumo", Permisos.ADMIN),
                        g("compra", Permisos.ADMIN), g("detalle_compra", Permisos.ADMIN), g("conversion_unidad", Permisos.ADMIN))),
                new Grupo("Reportes", List.of(i("Reportes (Excel y PDF)", "/reportes", Permisos.TODOS))));
        List<Grupo> visibles = new ArrayList<>();
        for (Grupo grupo : todos) {
            List<Item> items = grupo.items().stream()
                    .filter(it -> it.roles().stream().anyMatch(rolesUsuario::contains))
                    .toList();
            if (!items.isEmpty()) visibles.add(new Grupo(grupo.titulo(), items));
        }
        return visibles;
    }
}
