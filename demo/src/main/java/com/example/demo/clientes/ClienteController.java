package com.example.demo.clientes;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(@RequestParam(defaultValue = "") String q,
                        @RequestParam(defaultValue = "false") boolean inactivos, Model model) {
        model.addAttribute("clientes", service.buscar(q, inactivos));
        model.addAttribute("busqueda", q);
        model.addAttribute("inactivos", inactivos);
        model.addAttribute("totalClientes", service.cantidad());
        return "clientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return formulario(model, new ClienteForm(), List.of(), null);
    }

    @PostMapping
    public String crear(@ModelAttribute("cliente") ClienteForm cliente, Model model, RedirectAttributes redirect) {
        List<String> errores = service.validarNuevo(cliente);
        if (!errores.isEmpty()) return formulario(model, cliente, errores, null);
        service.crear(cliente);
        redirect.addFlashAttribute("mensaje", "Cliente registrado.");
        return "redirect:/clientes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model, RedirectAttributes redirect) {
        return service.buscarPorId(id)
                .map(cliente -> formulario(model, cliente, List.of(), id))
                .orElseGet(() -> noEncontrado(redirect));
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable int id, @ModelAttribute("cliente") ClienteForm cliente,
                             Model model, RedirectAttributes redirect) {
        List<String> errores = service.validarEdicion(id, cliente);
        if (!errores.isEmpty()) return formulario(model, cliente, errores, id);
        try {
            service.actualizar(id, cliente);
        } catch (NoSuchElementException e) {
            return noEncontrado(redirect);
        }
        redirect.addFlashAttribute("mensaje", "Cliente actualizado.");
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable int id, RedirectAttributes redirect) {
        try {
            service.desactivar(id);
            redirect.addFlashAttribute("mensaje", "Cliente desactivado. Sus pedidos y ventas se conservan.");
        } catch (NoSuchElementException e) {
            return noEncontrado(redirect);
        }
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable int id, RedirectAttributes redirect) {
        try {
            service.activar(id);
            redirect.addFlashAttribute("mensaje", "Cliente activado.");
        } catch (NoSuchElementException e) {
            return noEncontrado(redirect);
        }
        return "redirect:/clientes?inactivos=true";
    }

    @GetMapping("/{id}/historial")
    public String historial(@PathVariable int id, Model model, RedirectAttributes redirect) {
        return service.historial(id).map(h -> {
            model.addAttribute("historial", h);
            model.addAttribute("idCliente", id);
            return "clientes/historial";
        }).orElseGet(() -> noEncontrado(redirect));
    }

    private String formulario(Model model, ClienteForm cliente, List<String> errores, Integer id) {
        model.addAttribute("cliente", cliente);
        model.addAttribute("errores", errores);
        model.addAttribute("edicion", id != null);
        model.addAttribute("idCliente", id);
        return "clientes/formulario";
    }

    private String noEncontrado(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "No se encontró el cliente.");
        return "redirect:/clientes";
    }
}
