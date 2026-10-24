package com.example.demo.catalogo;

import java.math.BigDecimal;

public class ProductoForm {
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal precioMayorista;
    private Integer cantidadMinimaMayorista = 12;
    private String categoria;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getPrecioMayorista() {
        return precioMayorista;
    }

    public void setPrecioMayorista(BigDecimal precioMayorista) {
        this.precioMayorista = precioMayorista;
    }

    public Integer getCantidadMinimaMayorista() {
        return cantidadMinimaMayorista;
    }

    public void setCantidadMinimaMayorista(Integer cantidadMinimaMayorista) {
        this.cantidadMinimaMayorista = cantidadMinimaMayorista;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}