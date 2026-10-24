package com.example.demo.clientes;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Reglas de validación del formulario de cliente (RNF-09). No depende de la base de datos. */
@Component
public class ClienteValidador {
    private static final Pattern CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public List<String> validar(ClienteForm cliente) {
        List<String> errores = new ArrayList<>();
        if (StringUtils.isBlank(cliente.getNombre())) errores.add("Escribe el nombre del cliente.");

        String tipoDocumento = StringUtils.trimToEmpty(cliente.getTipoDocumento());
        String documento = StringUtils.trimToEmpty(cliente.getNumeroDocumento());
        if (tipoDocumento.isEmpty()) errores.add("Selecciona el tipo de documento.");
        if (documento.isEmpty()) {
            errores.add("Escribe el número de documento.");
        } else if ("DNI".equals(tipoDocumento) && !documento.matches("\\d{8}")) {
            errores.add("El DNI debe tener 8 dígitos.");
        } else if ("RUC".equals(tipoDocumento) && !documento.matches("\\d{11}")) {
            errores.add("El RUC debe tener 11 dígitos.");
        }

        if ("Empresa".equals(cliente.getTipoCliente()) && StringUtils.isBlank(cliente.getRazonSocial())) {
            errores.add("Escribe la razón social de la empresa.");
        }
        if (StringUtils.isNotBlank(cliente.getCorreo()) && !CORREO.matcher(cliente.getCorreo().trim()).matches()) {
            errores.add("Escribe un correo válido.");
        }
        return errores;
    }
}
