package com.example.demo.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
public class SeguridadConfig {
    @Bean
    SecurityFilterChain seguridadWeb(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(reglas -> reglas
                .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/favicon.ico", "/error").permitAll()
                // La primera regla que coincide manda: lo más específico va arriba.
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/gestion/*/*/eliminar").hasRole("ADMIN")
                .requestMatchers("/clientes/**").hasAnyRole(roles(Permisos.TODOS))
                .requestMatchers("/productos/nuevo", "/productos/*/editar", "/productos/*/variantes/nueva").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/productos/**").authenticated()
                .requestMatchers("/productos/**").hasAnyRole(roles(Permisos.ADMIN))
                .requestMatchers("/pedidos/**").hasAnyRole(roles(Permisos.TODOS))
                .requestMatchers("/reportes", "/reportes/pedidos").hasAnyRole(roles(Permisos.TODOS))
                .requestMatchers("/reportes/ventas").hasAnyRole(roles(Permisos.TODOS))
                .requestMatchers("/reportes/inventario").hasAnyRole(roles(Permisos.TODOS))
                .requestMatchers("/reportes/insumos-bajos").hasAnyRole(roles(Permisos.ADMIN))
                .requestMatchers(gestion(Permisos.CLAVES_TODOS)).hasAnyRole(roles(Permisos.TODOS))
                .requestMatchers(gestion(Permisos.CLAVES_ADMIN)).hasAnyRole(roles(Permisos.ADMIN))
                // Cualquier otra entidad de /gestion que no esté en la lista: solo administrador.
                .requestMatchers("/gestion/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll())
            .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static String[] roles(List<String> roles) {
        return roles.toArray(String[]::new);
    }

    private static String[] gestion(List<String> claves) {
        return claves.stream().map(c -> "/gestion/" + c + "/**").toArray(String[]::new);
    }
}
