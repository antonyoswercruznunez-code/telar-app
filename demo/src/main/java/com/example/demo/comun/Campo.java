package com.example.demo.comun;

import java.util.List;

/** Describe una columna de una tabla: cómo se muestra en la lista y en el formulario. */
public final class Campo {
    public enum Tipo { TEXTO, AREA, ENTERO, DECIMAL, FECHA, FECHAHORA, LISTA, FK }

    private final String nombre;
    private final String etiqueta;
    private final Tipo tipo;
    private boolean requerido;
    private boolean enLista = true;
    private boolean enFormulario = true;
    private boolean ancho;
    private List<String> opciones = List.of();
    private String lookup;
    private String defecto;

    private Campo(String nombre, String etiqueta, Tipo tipo) {
        this.nombre = nombre;
        this.etiqueta = etiqueta;
        this.tipo = tipo;
    }

    public static Campo texto(String n, String e) { return new Campo(n, e, Tipo.TEXTO); }
    public static Campo area(String n, String e) { return new Campo(n, e, Tipo.AREA).soloForm().ancha(); }
    public static Campo entero(String n, String e) { return new Campo(n, e, Tipo.ENTERO); }
    public static Campo decimal(String n, String e) { return new Campo(n, e, Tipo.DECIMAL); }
    public static Campo fecha(String n, String e) { return new Campo(n, e, Tipo.FECHA); }
    public static Campo fechaHora(String n, String e) { return new Campo(n, e, Tipo.FECHAHORA); }
    public static Campo lista(String n, String e, String... ops) {
        Campo c = new Campo(n, e, Tipo.LISTA);
        c.opciones = List.of(ops);
        return c;
    }
    /** @param lookup consulta SQL que devuelve las columnas «id» y «etiqueta». */
    public static Campo fk(String n, String e, String lookup) {
        Campo c = new Campo(n, e, Tipo.FK);
        c.lookup = lookup;
        return c;
    }

    public Campo req() { requerido = true; return this; }
    /** Campo calculado por el sistema: no aparece en el formulario. */
    public Campo oculto() { enFormulario = false; return this; }
    /** No aparece como columna en la lista. */
    public Campo soloForm() { enLista = false; return this; }
    public Campo ancha() { ancho = true; return this; }
    /** Valores especiales: "hoy", "ahora", "usuario" (usuario conectado); cualquier otro es literal. */
    public Campo def(String valor) { defecto = valor; return this; }

    public String nombre() { return nombre; }
    public String etiqueta() { return etiqueta; }
    public Tipo tipo() { return tipo; }
    public boolean requerido() { return requerido; }
    public boolean enLista() { return enLista; }
    public boolean enFormulario() { return enFormulario; }
    public boolean ancho() { return ancho; }
    public List<String> opciones() { return opciones; }
    public String lookup() { return lookup; }
    public String defecto() { return defecto; }
    public boolean buscable() { return tipo == Tipo.TEXTO || tipo == Tipo.LISTA; }
}
