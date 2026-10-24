package com.example.demo.seguridad;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {
    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) { this.service = service; }

    /** Roles disponibles en el desplegable (también al volver al formulario por errores). */
    @ModelAttribute("roles")
    public Rol[] roles() { return Rol.values(); }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new UsuarioForm());
        model.addAttribute("errores", List.of());
        return "usuarios/formulario";
    }

    @PostMapping
    public String crear(@ModelAttribute("usuario") UsuarioForm usuario,
                        Model model, RedirectAttributes redirect) {
        List<String> errores = validar(usuario);
        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            return "usuarios/formulario";
        }
        try {
            service.crear(usuario.getNombre(), usuario.getCorreo(), usuario.getClave(), usuario.getRol());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errores", List.of(ex.getMessage()));
            return "usuarios/formulario";
        }
        log.info("Usuario creado: {} con rol {}", usuario.getCorreo(), usuario.getRol());
        redirect.addFlashAttribute("mensaje", "Usuario creado. Su contraseña quedó cifrada.");
        return "redirect:/usuarios/nuevo";
    }

    private List<String> validar(UsuarioForm usuario) {
        List<String> errores = new ArrayList<>();
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            errores.add("Escribe el nombre del usuario.");
        }
        if (usuario.getCorreo() == null || !usuario.getCorreo().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            errores.add("Escribe un correo válido.");
        }
        if (usuario.getClave() == null || usuario.getClave().length() < 8) {
            errores.add("La contraseña debe tener al menos 8 caracteres.");
        }
        if (usuario.getClave() != null && !usuario.getClave().equals(usuario.getConfirmarClave())) {
            errores.add("Las contraseñas no coinciden.");
        }
        if (!Rol.esValido(usuario.getRol())) {
            errores.add("Selecciona un rol válido.");
        }
        return errores;
    }
}
