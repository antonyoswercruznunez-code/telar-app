package com.example.demo.seguridad;

import java.util.Arrays;

/** Roles del sistema. El nombre se guarda en la columna usuario.rol (Spring Security le antepone ROLE_). */
public enum Rol {
    ADMIN("Administrador"),
    VENDEDOR("Personal de ventas");

    private final String etiqueta;

    Rol(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }

    public static boolean esValido(String codigo) {
        return Arrays.stream(values()).anyMatch(r -> r.name().equals(codigo));
    }
}
