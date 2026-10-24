package com.example.demo.comun;

import java.util.List;

/** Error de negocio o de validación; revierte la transacción y se muestra al usuario. */
public class ValidacionException extends RuntimeException {
    private final List<String> errores;

    public ValidacionException(String mensaje) {
        super(mensaje);
        this.errores = List.of(mensaje);
    }

    public ValidacionException(List<String> errores) {
        super(String.join(" ", errores));
        this.errores = errores;
    }

    public List<String> errores() { return errores; }
}
