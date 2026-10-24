package com.example.demo.pedidos;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PedidoSeguimientoService {
    private static final Logger log = LoggerFactory.getLogger(PedidoSeguimientoService.class);

    private final PedidoSeguimientoRepository repository;

    public PedidoSeguimientoService(PedidoSeguimientoRepository repository) {
        this.repository = repository;
    }

    /** Lista filtrada; cada fila trae además la clave «siguiente» con el próximo estado del flujo (o null). */
    public List<Map<String, Object>> listar(String estado, LocalDate desde, LocalDate hasta, String texto) {
        Preconditions.checkArgument(estado == null || estado.isBlank() || EstadoPedido.de(estado).isPresent(),
                "Estado de pedido no válido.");
        Preconditions.checkArgument(desde == null || hasta == null || !hasta.isBefore(desde),
                "La fecha final no puede ser anterior a la inicial.");
        List<Map<String, Object>> filas = new ArrayList<>();
        for (Map<String, Object> fila : repository.listar(estado, desde, hasta, texto)) {
            Map<String, Object> copia = new java.util.LinkedHashMap<>(fila);
            copia.put("siguiente", siguienteDe((String) fila.get("estado")).orElse(null));
            filas.add(copia);
        }
        return filas;
    }

    public Optional<SeguimientoPedido> detalle(int id) {
        return repository.buscarPorId(id).map(pedido -> new SeguimientoPedido(
                pedido, siguienteDe((String) pedido.get("estado")).orElse(null),
                repository.productosDe(id), repository.produccionDe(id), repository.ventasDe(id)));
    }

    /** Pasa el pedido al siguiente estado del flujo normal. */
    @Transactional
    public String avanzar(int id) {
        String actual = repository.estadoDe(id).orElseThrow(() -> new NoSuchElementException("No se encontró el pedido " + id));
        String siguiente = siguienteDe(actual).orElseThrow(
                () -> new IllegalStateException("El pedido está «" + actual + "» y ya no puede avanzar."));
        repository.cambiarEstado(id, siguiente);
        log.info("Pedido {}: {} → {}", id, actual, siguiente);
        return siguiente;
    }

    /** Cambia el estado a cualquiera de los válidos (por ejemplo, para cancelar). */
    @Transactional
    public void cambiarEstado(int id, String nuevo) {
        EstadoPedido estado = EstadoPedido.de(nuevo)
                .orElseThrow(() -> new IllegalArgumentException("Estado de pedido no válido."));
        if (repository.cambiarEstado(id, estado.getEtiqueta()) == 0) {
            throw new NoSuchElementException("No se encontró el pedido " + id);
        }
        log.info("Pedido {} cambiado a {}", id, estado.getEtiqueta());
    }

    private Optional<String> siguienteDe(String etiqueta) {
        return EstadoPedido.de(etiqueta).flatMap(EstadoPedido::siguiente).map(EstadoPedido::getEtiqueta);
    }
}
