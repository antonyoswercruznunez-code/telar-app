package com.example.demo.catalogo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoValidadorTest {
    private final ProductoValidador validador = new ProductoValidador();

    private ProductoForm producto() {
        ProductoForm p = new ProductoForm();
        p.setNombre("Chalina de alpaca");
        p.setPrecio(new BigDecimal("80.00"));
        p.setPrecioMayorista(new BigDecimal("65.00"));
        return p;
    }

    @Test
    void productoCorrectoNoTieneErrores() {
        assertTrue(validador.validarProducto(producto()).isEmpty());
    }

    @Test
    void precioDebeSerMayorQueCero() {
        ProductoForm p = producto();
        p.setPrecio(BigDecimal.ZERO);
        assertTrue(validador.validarProducto(p).contains("El precio debe ser mayor que cero."));
    }

    @Test
    void precioMayoristaNoPuedeSerIgualOMayorAlNormal() {
        ProductoForm p = producto();
        p.setPrecioMayorista(new BigDecimal("80.00"));
        assertTrue(validador.validarProducto(p).contains("El precio mayorista debe ser menor que el precio normal."));
    }

    @Test
    void nombreEsObligatorio() {
        ProductoForm p = producto();
        p.setNombre("  ");
        assertTrue(validador.validarProducto(p).contains("Escribe el nombre del producto."));
    }

    @Test
    void varianteNecesitaColorTallaYStockNoNegativo() {
        VarianteForm v = new VarianteForm();
        v.setStockInicial(-1);
        var errores = validador.validarVariante(v);
        assertTrue(errores.contains("Escribe el color de la variante."));
        assertTrue(errores.contains("Escribe la talla de la variante."));
        assertTrue(errores.contains("El stock inicial no puede ser negativo."));
    }
}
