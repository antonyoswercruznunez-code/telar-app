package com.example.demo.clientes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ClienteService {
    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteRepository repository;
    private final ClienteValidador validador;

    public ClienteService(ClienteRepository repository, ClienteValidador validador) {
        this.repository = repository;
        this.validador = validador;
    }

    public List<Map<String, Object>> buscar(String texto, boolean incluirInactivos) {
        return repository.buscar(texto, incluirInactivos);
    }

    public int cantidad() {
        return repository.cantidad();
    }

    public Optional<ClienteForm> buscarPorId(int id) {
        return repository.buscarPorId(id);
    }

    /** Errores de un cliente nuevo: reglas del formulario + documento no repetido. */
    public List<String> validarNuevo(ClienteForm cliente) {
        List<String> errores = new ArrayList<>(validador.validar(cliente));
        String documento = cliente.getNumeroDocumento() == null ? "" : cliente.getNumeroDocumento().trim();
        if (!documento.isEmpty() && repository.existeDocumento(documento)) {
            errores.add("Ya existe un cliente con ese número de documento.");
        }
        return errores;
    }

    /** Errores al editar: igual que al crear, pero el cliente puede conservar su propio documento. */
    public List<String> validarEdicion(int id, ClienteForm cliente) {
        List<String> errores = new ArrayList<>(validador.validar(cliente));
        String documento = cliente.getNumeroDocumento() == null ? "" : cliente.getNumeroDocumento().trim();
        if (!documento.isEmpty() && repository.existeDocumentoDeOtro(documento, id)) {
            errores.add("Ya existe otro cliente con ese número de documento.");
        }
        return errores;
    }

    @Transactional
    public void crear(ClienteForm cliente) {
        repository.crear(cliente);
        log.info("Cliente registrado: documento {}", cliente.getNumeroDocumento());
    }

    @Transactional
    public void actualizar(int id, ClienteForm cliente) {
        if (repository.actualizar(id, cliente) == 0) throw new NoSuchElementException("No se encontró el cliente " + id);
        log.info("Cliente {} actualizado", id);
    }

    @Transactional
    public void desactivar(int id) {
        if (repository.cambiarEstado(id, false) == 0) throw new NoSuchElementException("No se encontró el cliente " + id);
        log.info("Cliente {} desactivado", id);
    }

    @Transactional
    public void activar(int id) {
        if (repository.cambiarEstado(id, true) == 0) throw new NoSuchElementException("No se encontró el cliente " + id);
        log.info("Cliente {} activado", id);
    }

    public Optional<HistorialCliente> historial(int id) {
        return repository.buscarPorId(id).map(cliente -> {
            List<Map<String, Object>> ventas = repository.ventasDe(id);
            BigDecimal total = BigDecimal.ZERO;
            BigDecimal saldo = BigDecimal.ZERO;
            for (Map<String, Object> venta : ventas) {
                total = total.add(decimal(venta.get("total")));
                saldo = saldo.add(decimal(venta.get("saldo")));
            }
            return new HistorialCliente(cliente, repository.pedidosDe(id), ventas, total, saldo);
        });
    }

    private static BigDecimal decimal(Object valor) {
        if (valor == null) return BigDecimal.ZERO;
        return valor instanceof BigDecimal b ? b : new BigDecimal(valor.toString());
    }
}
