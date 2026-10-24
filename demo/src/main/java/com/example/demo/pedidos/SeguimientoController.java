package com.example.demo.pedidos;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/pedidos")
public class SeguimientoController {
    private final PedidoSeguimientoService service;

    public SeguimientoController(PedidoSeguimientoService service) {
        this.service = service;
    }

    @GetMapping("/seguimiento")
    public String lista(@RequestParam(defaultValue = "") String estado,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                        @RequestParam(defaultValue = "") String q, Model model) {
        try {
            model.addAttribute("pedidos", service.listar(estado, desde, hasta, q));
        } catch (IllegalArgumentException e) {
            model.addAttribute("pedidos", java.util.List.of());
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("estados", EstadoPedido.etiquetas());
        model.addAttribute("estado", estado);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("q", q);
        return "pedidos/seguimiento";
    }

    @GetMapping("/{id}/seguimiento")
    public String detalle(@PathVariable int id, Model model, RedirectAttributes redirect) {
        return service.detalle(id).map(s -> {
            model.addAttribute("seguimiento", s);
            model.addAttribute("estados", EstadoPedido.etiquetas());
            return "pedidos/detalle";
        }).orElseGet(() -> {
            redirect.addFlashAttribute("error", "No se encontró el pedido.");
            return "redirect:/pedidos/seguimiento";
        });
    }

    @PostMapping("/{id}/avanzar")
    public String avanzar(@PathVariable int id, @RequestParam(defaultValue = "detalle") String volver,
                          RedirectAttributes redirect) {
        try {
            redirect.addFlashAttribute("mensaje", "Pedido pasó a «" + service.avanzar(id) + "».");
        } catch (NoSuchElementException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return destino(id, volver);
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable int id, @RequestParam String estado,
                                @RequestParam(defaultValue = "detalle") String volver, RedirectAttributes redirect) {
        try {
            service.cambiarEstado(id, estado);
            redirect.addFlashAttribute("mensaje", "Estado actualizado a «" + estado + "».");
        } catch (NoSuchElementException | IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return destino(id, volver);
    }

    private String destino(int id, String volver) {
        return "lista".equals(volver) ? "redirect:/pedidos/seguimiento" : "redirect:/pedidos/" + id + "/seguimiento";
    }
}
