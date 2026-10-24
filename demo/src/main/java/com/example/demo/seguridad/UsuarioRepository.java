package com.example.demo.seguridad;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepository {
    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<UsuarioAcceso> buscarPorCorreo(String correo) {
        List<UsuarioAcceso> usuarios = jdbc.query("""
            SELECT nombre, correo, contrasena, rol
            FROM usuario WHERE correo = ? LIMIT 1
            """, (rs, rowNum) -> new UsuarioAcceso(
                rs.getString("nombre"), rs.getString("correo"),
                rs.getString("contrasena"), rs.getString("rol")), correo);
        return usuarios.stream().findFirst();
    }

    public boolean existeCorreo(String correo) {
        Integer total = jdbc.queryForObject(
            "SELECT COUNT(*) FROM usuario WHERE correo = ?", Integer.class, correo);
        return total != null && total > 0;
    }

    public void crear(String nombre, String correo, String contrasenaCifrada, String rol) {
        jdbc.update("INSERT INTO usuario (nombre, correo, contrasena, rol) VALUES (?, ?, ?, ?)",
            nombre, correo, contrasenaCifrada, rol);
    }
}
