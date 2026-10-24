package com.example.demo.comun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Una tabla del sistema administrable desde /gestion/{clave}. */
public final class Entidad {
    /** Enlace a una tabla hija: /gestion/{clave}?f_{columna}={id del padre}. */
    public record Hijo(String etiqueta, String clave, String columna) { }

    private final String modulo;
    private final String clave;
    private final String titulo;
    private final String singular;
    private final String tabla;
    private final String pk;
    private final List<Campo> campos;
    private final List<Hijo> hijos = new ArrayList<>();

    private Entidad(String modulo, String clave, String titulo, String singular, String tabla, String pk, List<Campo> campos) {
        this.modulo = modulo;
        this.clave = clave;
        this.titulo = titulo;
        this.singular = singular;
        this.tabla = tabla;
        this.pk = pk;
        this.campos = campos;
    }

    public static Entidad de(String modulo, String clave, String titulo, String singular, String tabla, String pk, Campo... campos) {
        return new Entidad(modulo, clave, titulo, singular, tabla, pk, Arrays.asList(campos));
    }

    public Entidad hijo(String etiqueta, String clave, String columna) {
        hijos.add(new Hijo(etiqueta, clave, columna));
        return this;
    }

    public String modulo() { return modulo; }
    public String clave() { return clave; }
    public String titulo() { return titulo; }
    public String singular() { return singular; }
    public String tabla() { return tabla; }
    public String pk() { return pk; }
    public List<Campo> campos() { return campos; }
    public List<Hijo> hijos() { return hijos; }
}
