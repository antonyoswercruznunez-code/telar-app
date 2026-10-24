package com.example.demo.catalogo;

import com.example.demo.comun.Campo;
import com.example.demo.comun.Entidad;
import com.example.demo.comun.Lookups;
import com.example.demo.comun.ModuloCrud;
import org.springframework.stereotype.Component;

import java.util.List;

/** Módulo Catálogo de productos e inventario: producto y detalle_producto tienen pantallas propias; stock es genérico. */
@Component
public class CatalogoModulo implements ModuloCrud {
    private static final String M = "Catálogo de productos e inventario";

    @Override
    public List<Entidad> entidades() {
        return List.of(
                Entidad.de(M, "stock", "Stock", "Stock", "stock", "id_stock",
                        Campo.fk("id_detalle_producto", "Variante", Lookups.VARIANTES).req(),
                        Campo.entero("cantidad_disponible", "Cantidad disponible").req().def("0"),
                        Campo.entero("cantidad_minima", "Cantidad mínima").req().def("0")));
    }
}
