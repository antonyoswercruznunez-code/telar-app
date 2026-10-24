package com.example.demo.comun;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/** Pantallas genéricas (lista, alta, edición y baja) de todas las tablas, bajo /gestion/{clave}. */
@Controller
@RequestMapping("/gestion/{clave}")
public class CrudController {
    private final Entidades entidades;
    private final CrudService service;

    public CrudController(Entidades entidades, CrudService service) {
        this.entidades = entidades;
        this.service = service;
    }

    @GetMapping
    public String lista(@PathVariable String clave, @RequestParam Map<String, String> params, Model model) {
        Entidad e = entidades.requerir(clave);
        Map<String, String> filtros = service.filtros(e, params);
        String q = params.getOrDefault("q", "");
        Map<String, List<Opcion>> opciones = service.opciones(e);
        List<Map<String, String>> filas = service.listar(e, filtros, q, opciones);
        model.addAttribute("e", e);
        model.addAttribute("activo", "/gestion/" + clave);
        model.addAttribute("filas", filas);
        model.addAttribute("filtros", filtros);
        model.addAttribute("filtrosVista", service.describirFiltros(e, filtros, opciones));
        model.addAttribute("qs", qs(filtros));
        model.addAttribute("q", q);
        return "comun/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@PathVariable String clave, @RequestParam Map<String, String> params, Model model) {
        Entidad e = entidades.requerir(clave);
        Map<String, String> filtros = service.filtros(e, params);
        return formulario(model, e, filtros, service.valoresIniciales(e, filtros), List.of(), null);
    }

    @PostMapping
    public String crear(@PathVariable String clave, @RequestParam Map<String, String> params, Model model, RedirectAttributes redirect) {
        Entidad e = entidades.requerir(clave);
        Map<String, String> filtros = service.filtros(e, params);
        try {
            service.guardar(e, null, params);
            redirect.addFlashAttribute("mensaje", e.singular() + ": registro creado.");
            return "redirect:/gestion/" + clave + qs(filtros);
        } catch (ValidacionException ex) {
            return formulario(model, e, filtros, params, ex.errores(), null);
        } catch (DataAccessException ex) {
            return formulario(model, e, filtros, params, List.of("No se pudo guardar. Revisa que los datos relacionados existan y no estén repetidos."), null);
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String clave, @PathVariable int id, @RequestParam Map<String, String> params,
                         Model model, RedirectAttributes redirect) {
        Entidad e = entidades.requerir(clave);
        Map<String, String> filtros = service.filtros(e, params);
        try {
            return formulario(model, e, filtros, service.valoresDe(e, service.obtener(e, id)), List.of(), id);
        } catch (NoSuchElementException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/gestion/" + clave + qs(filtros);
        }
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable String clave, @PathVariable int id, @RequestParam Map<String, String> params,
                             Model model, RedirectAttributes redirect) {
        Entidad e = entidades.requerir(clave);
        Map<String, String> filtros = service.filtros(e, params);
        try {
            service.guardar(e, id, params);
            redirect.addFlashAttribute("mensaje", e.singular() + ": cambios guardados.");
            return "redirect:/gestion/" + clave + qs(filtros);
        } catch (ValidacionException ex) {
            return formulario(model, e, filtros, params, ex.errores(), id);
        } catch (NoSuchElementException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/gestion/" + clave + qs(filtros);
        } catch (DataAccessException ex) {
            return formulario(model, e, filtros, params, List.of("No se pudo guardar. Revisa que los datos relacionados existan y no estén repetidos."), id);
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String clave, @PathVariable int id, @RequestParam Map<String, String> params,
                           RedirectAttributes redirect) {
        Entidad e = entidades.requerir(clave);
        Map<String, String> filtros = service.filtros(e, params);
        try {
            service.eliminar(e, id);
            redirect.addFlashAttribute("mensaje", e.singular() + ": registro eliminado.");
        } catch (ValidacionException | NoSuchElementException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (DataAccessException ex) {
            redirect.addFlashAttribute("error", "No se puede eliminar: tiene registros relacionados.");
        }
        return "redirect:/gestion/" + clave + qs(filtros);
    }

    private String formulario(Model model, Entidad e, Map<String, String> filtros, Map<String, String> valores,
                              List<String> errores, Integer id) {
        model.addAttribute("e", e);
        model.addAttribute("activo", "/gestion/" + e.clave());
        model.addAttribute("opciones", service.opciones(e));
        model.addAttribute("valores", valores);
        model.addAttribute("filtros", filtros);
        model.addAttribute("qs", qs(filtros));
        model.addAttribute("errores", errores);
        model.addAttribute("id", id);
        return "comun/formulario";
    }

    private static String qs(Map<String, String> filtros) {
        if (filtros.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("?");
        filtros.forEach((k, v) -> {
            if (sb.length() > 1) sb.append('&');
            sb.append("f_").append(k).append('=').append(URLEncoder.encode(v, StandardCharsets.UTF_8));
        });
        return sb.toString();
    }
}
