package com.example.demo.catalogo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Reglas de validación de productos y variantes (RF-07, RNF-09). No depende de la base de datos. */
@Component
public class ProductoValidador {

    public List<String> validarProducto(ProductoForm producto) {
        List<String> errores = new ArrayList<>();
        if (StringUtils.isBlank(producto.getNombre())) errores.add("Escribe el nombre del producto.");

        BigDecimal precio = producto.getPrecio();
        BigDecimal mayorista = producto.getPrecioMayorista();
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            errores.add("El precio debe ser mayor que cero.");
        }
        if (mayorista != null) {
            if (mayorista.compareTo(BigDecimal.ZERO) <= 0) {
                errores.add("El precio mayorista debe ser mayor que cero.");
            } else if (precio != null && precio.signum() > 0 && mayorista.compareTo(precio) >= 0) {
                errores.add("El precio mayorista debe ser menor que el precio normal.");
            }
        }
        if (producto.getCantidadMinimaMayorista() != null && producto.getCantidadMinimaMayorista() < 1) {
            errores.add("La cantidad mínima mayorista debe ser al menos 1.");
        }
        return errores;
    }

    public List<String> validarVariante(VarianteForm variante) {
        List<String> errores = new ArrayList<>();
        if (StringUtils.isBlank(variante.getColor())) errores.add("Escribe el color de la variante.");
        if (StringUtils.isBlank(variante.getTalla())) errores.add("Escribe la talla de la variante.");
        if (variante.getStockInicial() != null && variante.getStockInicial() < 0)
            errores.add("El stock inicial no puede ser negativo.");
        if (variante.getStockMinimo() != null && variante.getStockMinimo() < 0)
            errores.add("El stock mínimo no puede ser negativo.");
        return errores;
    }
}
