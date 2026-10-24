package com.example.demo.clientes;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** DAO de clientes. Todas las consultas usan parámetros (?) para evitar inyección SQL. */
@Repository
public class ClienteRepository {
    private final JdbcTemplate jdbc;

    public ClienteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> buscar(String busqueda, boolean incluirInactivos) {
        String termino = "%" + (busqueda == null ? "" : busqueda.trim()) + "%";
        return jdbc.queryForList("""
                SELECT id_cliente AS idCliente, tipo_cliente AS tipoCliente,
                       razon_social AS razonSocial, nombre, apellido, telefono,
                       correo, Tipo_Documento AS tipoDocumento,
                       Numero_Documento AS numeroDocumento, activo
                FROM cliente
                WHERE (CONCAT_WS(' ', nombre, apellido, razon_social) LIKE ?
                   OR Numero_Documento LIKE ? OR COALESCE(telefono, '') LIKE ?)
                  AND (activo = 1 OR ? = 1)
                ORDER BY nombre, apellido
                """, termino, termino, termino, incluirInactivos ? 1 : 0);
    }

    /** Cantidad de clientes activos. */
    public int cantidad() {
        Integer cantidad = jdbc.queryForObject("SELECT COUNT(*) FROM cliente WHERE activo = 1", Integer.class);
        return cantidad == null ? 0 : cantidad;
    }

    public Optional<ClienteForm> buscarPorId(int id) {
        List<ClienteForm> filas = jdbc.query("""
                SELECT tipo_cliente, razon_social, nombre, apellido, telefono, correo,
                       persona_contacto, direccion, Tipo_Documento, Numero_Documento
                FROM cliente WHERE id_cliente = ?
                """, (rs, n) -> {
            ClienteForm f = new ClienteForm();
            f.setTipoCliente(rs.getString("tipo_cliente"));
            f.setRazonSocial(rs.getString("razon_social"));
            f.setNombre(rs.getString("nombre"));
            f.setApellido(rs.getString("apellido"));
            f.setTelefono(rs.getString("telefono"));
            f.setCorreo(rs.getString("correo"));
            f.setPersonaContacto(rs.getString("persona_contacto"));
            f.setDireccion(rs.getString("direccion"));
            f.setTipoDocumento(rs.getString("Tipo_Documento"));
            f.setNumeroDocumento(rs.getString("Numero_Documento"));
            return f;
        }, id);
        return filas.stream().findFirst();
    }

    public boolean existeDocumento(String numeroDocumento) {
        Integer cantidad = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cliente WHERE Numero_Documento = ?", Integer.class, numeroDocumento);
        return cantidad != null && cantidad > 0;
    }

    /** ¿Otro cliente (distinto de {@code id}) ya usa ese documento? */
    public boolean existeDocumentoDeOtro(String numeroDocumento, int id) {
        Integer cantidad = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cliente WHERE Numero_Documento = ? AND id_cliente <> ?",
                Integer.class, numeroDocumento, id);
        return cantidad != null && cantidad > 0;
    }

    public void crear(ClienteForm cliente) {
        jdbc.update("""
                        INSERT INTO cliente (tipo_cliente, razon_social, nombre, apellido, telefono,
                                             correo, persona_contacto, direccion, Tipo_Documento, Numero_Documento)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """, normalizar(cliente.getTipoCliente(), "Persona"), vacioANull(cliente.getRazonSocial()),
                cliente.getNombre().trim(), vacioANull(cliente.getApellido()), vacioANull(cliente.getTelefono()),
                vacioANull(cliente.getCorreo()), vacioANull(cliente.getPersonaContacto()),
                vacioANull(cliente.getDireccion()), cliente.getTipoDocumento().trim(), cliente.getNumeroDocumento().trim());
    }

    /** @return filas afectadas (0 si el cliente no existe) */
    public int actualizar(int id, ClienteForm cliente) {
        return jdbc.update("""
                        UPDATE cliente SET tipo_cliente = ?, razon_social = ?, nombre = ?, apellido = ?, telefono = ?,
                                           correo = ?, persona_contacto = ?, direccion = ?, Tipo_Documento = ?,
                                           Numero_Documento = ?
                        WHERE id_cliente = ?
                        """, normalizar(cliente.getTipoCliente(), "Persona"), vacioANull(cliente.getRazonSocial()),
                cliente.getNombre().trim(), vacioANull(cliente.getApellido()), vacioANull(cliente.getTelefono()),
                vacioANull(cliente.getCorreo()), vacioANull(cliente.getPersonaContacto()),
                vacioANull(cliente.getDireccion()), cliente.getTipoDocumento().trim(),
                cliente.getNumeroDocumento().trim(), id);
    }

    /** @return filas afectadas (0 si el cliente no existe) */
    public int cambiarEstado(int id, boolean activo) {
        return jdbc.update("UPDATE cliente SET activo = ? WHERE id_cliente = ?", activo ? 1 : 0, id);
    }

    public List<Map<String, Object>> pedidosDe(int idCliente) {
        return jdbc.queryForList("""
                SELECT id_pedido AS id, DATE_FORMAT(fecha_pedido, '%d/%m/%Y %H:%i') AS fecha,
                       descripcion, estado, DATE_FORMAT(fecha_entrega, '%d/%m/%Y') AS entrega
                FROM pedido WHERE id_cliente = ? ORDER BY fecha_pedido DESC
                """, idCliente);
    }

    public List<Map<String, Object>> ventasDe(int idCliente) {
        return jdbc.queryForList("""
                SELECT id_venta AS id, DATE_FORMAT(fecha_venta, '%d/%m/%Y %H:%i') AS fecha,
                       tipo_venta AS tipo, total, saldo_pendiente AS saldo, estado_pago AS estadoPago
                FROM venta WHERE id_cliente = ? ORDER BY fecha_venta DESC
                """, idCliente);
    }

    private String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String normalizar(String valor, String predeterminado) {
        return valor == null || valor.isBlank() ? predeterminado : valor.trim();
    }
}
