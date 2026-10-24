package com.example.demo.seguridad;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolTest {
    @Test
    void aceptaSoloLosDosRolesDelSrs() {
        assertTrue(Rol.esValido("ADMIN"));
        assertTrue(Rol.esValido("VENDEDOR"));
        assertEquals(2, Rol.values().length);
    }

    @Test
    void rechazaRolesInventadosOVacios() {
        assertFalse(Rol.esValido("SUPERADMIN"));
        assertFalse(Rol.esValido("ALMACEN"));
        assertFalse(Rol.esValido("admin"));
        assertFalse(Rol.esValido(null));
    }
}
