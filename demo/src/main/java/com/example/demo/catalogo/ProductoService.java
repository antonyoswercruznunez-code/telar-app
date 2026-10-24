package com.example.demo.catalogo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductoService {
    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository repository;
    private final ProductoValidador validador;

    public ProductoService(ProductoRepository repository, ProductoValidador validador) {
        this.repository = repository;
        this.validador = validador;
    }

    public List<Map<String, Object>> buscarTodos(String busqueda, boolean incluirInactivos) {
        return repository.buscarTodos(busqueda, incluirInactivos);
    }

    /** Errores de un producto nuevo con su primera variante: reglas del formulario + nombre no repetido (RF-07). */
    public List<String> validarNuevo(ProductoForm producto, VarianteForm variante) {
        List<String> errores = new ArrayList<>(validador.validarProducto(producto));
        errores.addAll(validador.validarVariante(variante));
        if (StringUtils.isNotBlank(producto.getNombre()) && repository.existeNombre(producto.getNombre().trim(), 0)) {
            errores.add("Ya existe un producto con ese nombre.");
        }
        return errores;
    }

    public List<String> validarEdicion(int id, ProductoForm producto) {
        List<String> errores = new ArrayList<>(validador.validarProducto(producto));
        if (StringUtils.isNotBlank(producto.getNombre()) && repository.existeNombre(producto.getNombre().trim(), id)) {
            errores.add("Ya existe otro producto con ese nombre.");
        }
        return errores;
    }

    public List<String> validarVariante(VarianteForm variante) {
        return validador.validarVariante(variante);
    }

    @Transactional
    public void desactivar(int id) {
        if (repository.cambiarEstado(id, false) == 0) throw new NoSuchElementException("No se encontró el producto " + id);
        log.info("Producto {} desactivado", id);
    }

    @Transactional
    public void activar(int id) {
        if (repository.cambiarEstado(id, true) == 0) throw new NoSuchElementException("No se encontró el producto " + id);
        log.info("Producto {} activado", id);
    }

    public Optional<Map<String, Object>> buscarPorId(int id) {
        return repository.buscarPorId(id);
    }

    public List<Map<String, Object>> buscarVariantes(int id) {
        return repository.buscarVariantes(id);
    }

    public int cantidadProductos() {
        return repository.cantidadProductos();
    }

    @Transactional
    public int crearProducto(ProductoForm producto, VarianteForm variante) {
        int idProducto = repository.crearProducto(producto);
        int idVariante = repository.crearVariante(idProducto, variante);
        repository.crearStock(idVariante, variante);
        return idProducto;
    }

    @Transactional
    public void actualizarProducto(int id, ProductoForm producto) {
        if (!repository.existe(id)) throw new IllegalArgumentException("No se encontró el producto.");
        repository.actualizarProducto(id, producto);
    }

    @Transactional
    public void agregarVariante(int idProducto, VarianteForm variante) {
        if (!repository.existe(idProducto)) throw new IllegalArgumentException("No se encontró el producto.");
        int idVariante = repository.crearVariante(idProducto, variante);
        repository.crearStock(idVariante, variante);
    }
}