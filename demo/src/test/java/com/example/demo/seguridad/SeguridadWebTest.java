package com.example.demo.seguridad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.junit.jupiter.api.AfterEach;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SeguridadWebTest {
    private AnnotationConfigWebApplicationContext contexto;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        contexto = new AnnotationConfigWebApplicationContext();
        contexto.setServletContext(new MockServletContext());
        contexto.register(Config.class);
        contexto.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @AfterEach
    void cerrar() { contexto.close(); }

    @Test
    void anonimoDebeIniciarSesion() throws Exception {
        mvc.perform(get("/inventario")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void vendedorNoAdministraUsuarios() throws Exception {
        mvc.perform(get("/usuarios/lista").with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorAccedeAUsuarios() throws Exception {
        mvc.perform(get("/usuarios/lista").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void vendedorPuedeConsultarInventario() throws Exception {
        mvc.perform(get("/inventario").with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isOk());
    }

    @Test
    void logoutSinCsrfSeRechaza() throws Exception {
        mvc.perform(post("/logout").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutConCsrfCierraSesion() throws Exception {
        mvc.perform(post("/logout").with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void bcryptNoAlmacenaLaClaveEnTextoPlano() {
        var encoder = contexto.getBean(org.springframework.security.crypto.password.PasswordEncoder.class);
        var hash = encoder.encode("clave-de-prueba");
        org.junit.jupiter.api.Assertions.assertNotEquals("clave-de-prueba", hash);
        org.junit.jupiter.api.Assertions.assertTrue(encoder.matches("clave-de-prueba", hash));
        org.junit.jupiter.api.Assertions.assertFalse(encoder.matches("otra-clave", hash));
    }

    @Configuration
    @EnableWebSecurity
    @EnableWebMvc
    @Import(SeguridadConfig.class)
    static class Config {
        @Bean UserDetailsService usuarios() { return new InMemoryUserDetailsManager(); }
        @Bean Endpoints endpoints() { return new Endpoints(); }
    }

    @RestController
    static class Endpoints {
        @GetMapping({"/inventario", "/usuarios/lista"})
        String consultar() { return "ok"; }
    }
}
