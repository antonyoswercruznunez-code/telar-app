package com.example.demo.reportes;

import com.example.demo.pedidos.EstadoPedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.util.function.Function;

@Controller
@RequestMapping("/reportes")
public class ReporteController {
    private static final Logger log = LoggerFactory.getLogger(ReporteController.class);

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    /** Pantalla con los reportes disponibles. */
    @GetMapping
    public String pagina(Model model) {
        LocalDate hoy = LocalDate.now();
        model.addAttribute("desde", hoy.withDayOfMonth(1));
        model.addAttribute("hasta", hoy);
        model.addAttribute("estados", EstadoPedido.etiquetas());
        return "reportes/index";
    }

    @GetMapping("/inventario")
    public ResponseEntity<byte[]> inventario(@RequestParam(required = false) String formato, Principal usuario) {
        return descargar("inventario", formato, usuario, service::inventario);
    }

    /** Sin fechas: ventas del mes en curso. Ejemplo: /reportes/ventas?desde=2026-10-01&hasta=2026-10-31&formato=pdf */
    @GetMapping("/ventas")
    public ResponseEntity<byte[]> ventas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String formato, Principal usuario) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);
        return descargar("ventas_" + inicio + "_" + fin, formato, usuario, f -> service.ventas(inicio, fin, f));
    }

    @GetMapping("/pedidos")
    public ResponseEntity<byte[]> pedidos(@RequestParam(defaultValue = "") String estado,
                                          @RequestParam(required = false) String formato, Principal usuario) {
        return descargar("pedidos", formato, usuario, f -> service.pedidos(estado, f));
    }

    @GetMapping("/insumos-bajos")
    public ResponseEntity<byte[]> insumosBajos(@RequestParam(required = false) String formato, Principal usuario) {
        return descargar("insumos_stock_bajo", formato, usuario, service::insumosBajos);
    }

    private ResponseEntity<byte[]> descargar(String nombre, String formatoTexto, Principal usuario,
                                             Function<FormatoReporte, byte[]> generador) {
        try {
            FormatoReporte formato = FormatoReporte.de(formatoTexto);
            byte[] datos = generador.apply(formato);
            log.info("{} descarga el reporte {}.{}", usuario.getName(), nombre, formato.extension());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(nombre + "." + formato.extension()).build().toString())
                    .contentType(MediaType.parseMediaType(formato.tipoContenido()))
                    .body(datos);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
