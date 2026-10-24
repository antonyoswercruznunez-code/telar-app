package com.example.demo.catalogo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/productos")
public class ProductoController {
    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(@RequestParam(defaultValue = "") String q,
                        @RequestParam(defaultValue = "false") boolean inactivos, Model model) {
        model.addAttribute("productos", service.buscarTodos(q, inactivos));
        model.addAttribute("busqueda", q);
        model.addAttribute("inactivos", inactivos);
        model.addAttribute("totalProductos", service.cantidadProductos());
        return "catalogo/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("producto", new ProductoForm());
        model.addAttribute("variante", new VarianteForm());
        model.addAttribute("edicion", false);
        model.addAttribute("errores", List.of());
        return "catalogo/formulario";
    }

    @PostMapping
    public String crear(@ModelAttribute("producto") ProductoForm producto,
                        @ModelAttribute("variante") VarianteForm variante,
                        Model model, RedirectAttributes redirect) {
        List<String> errores = service.validarNuevo(producto, variante);
        if (!errores.isEmpty()) {
            model.addAttribute("producto", producto);
            model.addAttribute("variante", variante);
            model.addAttribute("edicion", false);
            model.addAttribute("errores", errores);
            return "catalogo/formulario";
        }
        service.crearProducto(producto, variante);
        redirect.addFlashAttribute("mensaje", "Producto y variante registrados.");
        return "redirect:/productos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model) {
        Map<String, Object> fila = service.buscarPorId(id).orElse(null);
        if (fila == null) return "redirect:/productos";
        ProductoForm form = new ProductoForm();
        form.setNombre((String) fila.get("nombre"));
        form.setDescripcion((String) fila.get("descripcion"));
        form.setPrecio((BigDecimal) fila.get("precio"));
        form.setPrecioMayorista((BigDecimal) fila.get("precioMayorista"));
        form.setCantidadMinimaMayorista((Integer) fila.get("cantidadMinimaMayorista"));
        form.setCategoria((String) fila.get("categoria"));
        model.addAttribute("producto", form);
        model.addAttribute("idProducto", id);
        model.addAttribute("edicion", true);
        model.addAttribute("errores", List.of());
        return "catalogo/formulario";
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable int id, @ModelAttribute("producto") ProductoForm producto,
                             Model model, RedirectAttributes redirect) {
        List<String> errores = service.validarEdicion(id, producto);
        if (!errores.isEmpty()) {
            model.addAttribute("producto", producto);
            model.addAttribute("idProducto", id);
            model.addAttribute("edicion", true);
            model.addAttribute("errores", errores);
            return "catalogo/formulario";
        }
        service.actualizarProducto(id, producto);
        redirect.addFlashAttribute("mensaje", "Producto actualizado.");
        return "redirect:/productos";
    }

    @GetMapping("/{id}/variantes")
    public String variantes(@PathVariable int id, Model model) {
        Map<String, Object> producto = service.buscarPorId(id).orElse(null);
        if (producto == null) return "redirect:/productos";
        model.addAttribute("producto", producto);
        model.addAttribute("variantes", service.buscarVariantes(id));
        return "catalogo/variantes";
    }

    @GetMapping("/{id}/variantes/nueva")
    public String nuevaVariante(@PathVariable int id, Model model) {
        Map<String, Object> producto = service.buscarPorId(id).orElse(null);
        if (producto == null) return "redirect:/productos";
        model.addAttribute("producto", producto);
        model.addAttribute("variante", new VarianteForm());
        model.addAttribute("errores", List.of());
        return "catalogo/variante-formulario";
    }

    @PostMapping("/{id}/variantes")
    public String agregarVariante(@PathVariable int id, @ModelAttribute("variante") VarianteForm variante,
                                  Model model, RedirectAttributes redirect) {
        Map<String, Object> producto = service.buscarPorId(id).orElse(null);
        if (producto == null) return "redirect:/productos";
        List<String> errores = service.validarVariante(variante);
        if (!errores.isEmpty()) {
            model.addAttribute("producto", producto);
            model.addAttribute("variante", variante);
            model.addAttribute("errores", errores);
            return "catalogo/variante-formulario";
        }
        service.agregarVariante(id, variante);
        redirect.addFlashAttribute("mensaje", "Variante y stock registrados.");
        return "redirect:/productos/" + id + "/variantes";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable int id, RedirectAttributes redirect) {
        try {
            service.desactivar(id);
            redirect.addFlashAttribute("mensaje", "Producto desactivado. Ya no aparece en ventas, pedidos ni inventario.");
        } catch (java.util.NoSuchElementException e) {
            redirect.addFlashAttribute("error", "No se encontró el producto.");
        }
        return "redirect:/productos";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable int id, RedirectAttributes redirect) {
        try {
            service.activar(id);
            redirect.addFlashAttribute("mensaje", "Producto activado.");
        } catch (java.util.NoSuchElementException e) {
            redirect.addFlashAttribute("error", "No se encontró el producto.");
        }
        return "redirect:/productos?inactivos=true";
    }
}
