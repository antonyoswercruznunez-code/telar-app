package com.example.demo.comun;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuarioActual {
    private final JdbcTemplate jdbc;

    public UsuarioActual(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Id del usuario conectado (el nombre de usuario de Spring Security es su correo). */
    public Integer id() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) return null;
        List<Integer> ids = jdbc.queryForList("SELECT id_usuario FROM usuario WHERE correo = ?", Integer.class, a.getName());
        return ids.isEmpty() ? null : ids.get(0);
    }
}
