package com.example.demo.catalogo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InventarioController {
    private final InventarioService servicio;

    public InventarioController(InventarioService servicio) { this.servicio = servicio; }

    @GetMapping("/inventario")
    public String inventario(Model model) {
        var resumen = servicio.consultar();
        model.addAttribute("articulos", resumen.articulos());
        model.addAttribute("totalProductos", resumen.totalProductos());
        model.addAttribute("totalVariantes", resumen.totalVariantes());
        model.addAttribute("alertasStock", resumen.alertasStock());
        return "catalogo/inventario";
    }
}
