package com.example.demo.clientes;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteValidadorTest {
    private final ClienteValidador validador = new ClienteValidador();

    private ClienteForm valido() {
        ClienteForm c = new ClienteForm();
        c.setNombre("Rosa");
        c.setTipoDocumento("DNI");
        c.setNumeroDocumento("45871236");
        return c;
    }

    @Test
    void clienteCorrectoNoTieneErrores() {
        assertTrue(validador.validar(valido()).isEmpty());
    }

    @Test
    void nombreYDocumentoSonObligatorios() {
        ClienteForm c = new ClienteForm();
        List<String> errores = validador.validar(c);
        assertTrue(errores.contains("Escribe el nombre del cliente."));
        assertTrue(errores.contains("Escribe el número de documento."));
    }

    @Test
    void dniDebeTenerOchoDigitos() {
        ClienteForm c = valido();
        c.setNumeroDocumento("1234");
        assertTrue(validador.validar(c).contains("El DNI debe tener 8 dígitos."));
    }

    @Test
    void rucDebeTenerOnceDigitos() {
        ClienteForm c = valido();
        c.setTipoDocumento("RUC");
        c.setNumeroDocumento("2060123456");
        assertTrue(validador.validar(c).contains("El RUC debe tener 11 dígitos."));
    }

    @Test
    void empresaNecesitaRazonSocial() {
        ClienteForm c = valido();
        c.setTipoCliente("Empresa");
        assertTrue(validador.validar(c).contains("Escribe la razón social de la empresa."));
    }

    @Test
    void correoInvalidoSeRechazaPeroVacioSePermite() {
        ClienteForm c = valido();
        c.setCorreo("sin-arroba");
        assertTrue(validador.validar(c).contains("Escribe un correo válido."));
        c.setCorreo("");
        assertTrue(validador.validar(c).isEmpty());
    }
}
