package com.example.demo.seguridad;

import java.util.Locale;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository repository;

    public UsuarioDetailsService(UsuarioRepository repository) { this.repository = repository; }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        UsuarioAcceso usuario = repository.buscarPorCorreo(correo.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new UsernameNotFoundException("Credenciales no válidas."));
        String rol = usuario.rol() == null ? "USER" : usuario.rol().trim().toUpperCase(Locale.ROOT);
        if (!rol.startsWith("ROLE_")) rol = "ROLE_" + rol;
        return User.withUsername(usuario.correo())
            .password(usuario.contrasena())
            .authorities(new SimpleGrantedAuthority(rol))
            .build();
    }
}