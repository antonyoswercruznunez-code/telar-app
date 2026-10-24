package com.example.demo.seguridad;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void crear(String nombre, String correo, String clave, String rol) {
        String correoNormalizado = correo.trim().toLowerCase(Locale.ROOT);
        if (repository.existeCorreo(correoNormalizado)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
        }
        repository.crear(nombre.trim(), correoNormalizado, passwordEncoder.encode(clave), rol);
    }
}
