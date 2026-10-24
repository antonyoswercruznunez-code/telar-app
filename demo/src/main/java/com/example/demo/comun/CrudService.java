package com.example.demo.comun;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/** Lógica común de listado, alta, edición y baja para todas las entidades. */
@Service
public class CrudService {
    private static final DateTimeFormatter F_FORM = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter F_VER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JdbcTemplate jdbc;
    private final List<ModuloCrud> modulos;
    private final UsuarioActual usuarioActual;

    public CrudService(JdbcTemplate jdbc, List<ModuloCrud> modulos, UsuarioActual usuarioActual) {
        this.jdbc = jdbc;
        this.modulos = modulos;
        this.usuarioActual = usuarioActual;
    }

    // ---------- Consultas ----------

    /** Filtros f_{columna}=valor recibidos en la URL; solo se aceptan columnas de la entidad. */
    public Map<String, String> filtros(Entidad e, Map<String, String> params) {
        Map<String, String> r = new LinkedHashMap<>();
        for (Campo c : e.campos()) {
            String v = params.get("f_" + c.nombre());
            if (v != null && !v.isBlank()) r.put(c.nombre(), v.trim());
        }
        return r;
    }

    public Map<String, List<Opcion>> opciones(Entidad e) {
        Map<String, List<Opcion>> r = new LinkedHashMap<>();
        for (Campo c : e.campos()) {
            if (c.tipo() != Campo.Tipo.FK) continue;
            List<Opcion> lista = new ArrayList<>();
            for (Map<String, Object> fila : jdbc.queryForList(c.lookup())) {
                String id = String.valueOf(fila.get("id"));
                Object et = fila.get("etiqueta");
                lista.add(new Opcion(id, et == null ? "#" + id : et.toString()));
            }
            r.put(c.nombre(), lista);
        }
        return r;
    }

    public List<String> describirFiltros(Entidad e, Map<String, String> filtros, Map<String, List<Opcion>> opciones) {
        List<String> r = new ArrayList<>();
        for (Map.Entry<String, String> f : filtros.entrySet()) {
            Campo c = campo(e, f.getKey());
            String valor = f.getValue();
            for (Opcion o : opciones.getOrDefault(f.getKey(), List.of())) {
                if (o.id().equals(valor)) valor = o.etiqueta();
            }
            r.add(c.etiqueta() + ": " + valor);
        }
        return r;
    }

    public List<Map<String, String>> listar(Entidad e, Map<String, String> filtros, String q, Map<String, List<Opcion>> opciones) {
        StringBuilder sql = new StringBuilder("SELECT * FROM `" + e.tabla() + "` WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        for (Map.Entry<String, String> f : filtros.entrySet()) {
            sql.append(" AND `").append(f.getKey()).append("` = ?");
            args.add(f.getValue());
        }
        if (q != null && !q.isBlank()) {
            List<String> cols = new ArrayList<>();
            for (Campo c : e.campos()) if (c.buscable()) cols.add("`" + c.nombre() + "`");
            if (!cols.isEmpty()) {
                sql.append(" AND CONCAT_WS(' ', ").append(String.join(", ", cols)).append(") LIKE ?");
                args.add("%" + q.trim() + "%");
            }
        }
        sql.append(" ORDER BY `").append(e.pk()).append("` DESC LIMIT 500");

        Map<String, Map<String, String>> etiquetas = new LinkedHashMap<>();
        opciones.forEach((col, lista) -> {
            Map<String, String> m = new LinkedHashMap<>();
            lista.forEach(o -> m.put(o.id(), o.etiqueta()));
            etiquetas.put(col, m);
        });

        List<Map<String, String>> filas = new ArrayList<>();
        for (Map<String, Object> row : jdbc.queryForList(sql.toString(), args.toArray())) {
            Map<String, String> fila = new LinkedHashMap<>();
            fila.put("__id", String.valueOf(row.get(e.pk())));
            for (Campo c : e.campos()) fila.put(c.nombre(), mostrar(c, row.get(c.nombre()), etiquetas));
            filas.add(fila);
        }
        return filas;
    }

    public Map<String, Object> obtener(Entidad e, int id) {
        List<Map<String, Object>> filas = jdbc.queryForList("SELECT * FROM `" + e.tabla() + "` WHERE `" + e.pk() + "` = ?", id);
        if (filas.isEmpty()) throw new NoSuchElementException("El registro ya no existe.");
        return filas.get(0);
    }

    public Map<String, String> valoresIniciales(Entidad e, Map<String, String> filtros) {
        Map<String, String> r = new LinkedHashMap<>();
        Integer uid = null;
        boolean uidCargado = false;
        for (Campo c : e.campos()) {
            if (!c.enFormulario()) continue;
            String d = c.defecto();
            String valor = "";
            if ("hoy".equals(d)) valor = LocalDate.now().toString();
            else if ("ahora".equals(d)) valor = LocalDateTime.now().format(F_FORM);
            else if ("usuario".equals(d)) {
                if (!uidCargado) { uid = usuarioActual.id(); uidCargado = true; }
                valor = uid == null ? "" : uid.toString();
            } else if (d != null) valor = d;
            r.put(c.nombre(), valor);
        }
        r.putAll(filtros);
        return r;
    }

    public Map<String, String> valoresDe(Entidad e, Map<String, Object> row) {
        Map<String, String> r = new LinkedHashMap<>();
        for (Campo c : e.campos()) {
            if (c.enFormulario()) r.put(c.nombre(), paraFormulario(c, row.get(c.nombre())));
        }
        return r;
    }

    // ---------- Escritura ----------

    @Transactional
    public int guardar(Entidad e, Integer id, Map<String, String> params) {
        List<String> errores = new ArrayList<>();
        Map<String, Object> v = new LinkedHashMap<>();
        for (Campo c : e.campos()) {
            if (!c.enFormulario()) continue;
            String raw = params.get(c.nombre());
            raw = raw == null || raw.isBlank() ? null : raw.trim();
            if (raw == null) {
                if (c.requerido()) errores.add("Completa el campo «" + c.etiqueta() + "».");
                v.put(c.nombre(), null);
                continue;
            }
            try {
                v.put(c.nombre(), convertir(c, raw));
            } catch (ValidacionException ex) {
                errores.addAll(ex.errores());
            } catch (RuntimeException ex) {
                errores.add("El valor de «" + c.etiqueta() + "» no es válido.");
            }
        }
        if (!errores.isEmpty()) throw new ValidacionException(errores);

        Map<String, Object> anterior = id == null ? null : obtener(e, id);
        for (ModuloCrud m : modulos) m.antes(e, v, anterior);

        List<String> cols = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        for (Campo c : e.campos()) {
            if (!v.containsKey(c.nombre())) continue;
            Object valor = v.get(c.nombre());
            if (anterior == null && valor == null) continue; // al crear, la base aplica su valor por defecto
            cols.add(c.nombre());
            args.add(valor);
        }

        int idFinal;
        if (anterior == null) {
            StringBuilder sql = new StringBuilder("INSERT INTO `" + e.tabla() + "` (");
            StringBuilder marcas = new StringBuilder();
            for (int i = 0; i < cols.size(); i++) {
                if (i > 0) { sql.append(", "); marcas.append(", "); }
                sql.append('`').append(cols.get(i)).append('`');
                marcas.append('?');
            }
            sql.append(") VALUES (").append(marcas).append(')');
            KeyHolder key = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < args.size(); i++) ps.setObject(i + 1, args.get(i));
                return ps;
            }, key);
            idFinal = key.getKey().intValue();
        } else {
            StringBuilder sql = new StringBuilder("UPDATE `" + e.tabla() + "` SET ");
            for (int i = 0; i < cols.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append('`').append(cols.get(i)).append("` = ?");
            }
            sql.append(" WHERE `").append(e.pk()).append("` = ?");
            args.add(id);
            jdbc.update(sql.toString(), args.toArray());
            idFinal = id;
        }
        for (ModuloCrud m : modulos) m.despues(e, idFinal, v, anterior);
        return idFinal;
    }

    @Transactional
    public void eliminar(Entidad e, int id) {
        Map<String, Object> anterior = obtener(e, id);
        for (ModuloCrud m : modulos) m.antesEliminar(e, anterior);
        jdbc.update("DELETE FROM `" + e.tabla() + "` WHERE `" + e.pk() + "` = ?", id);
        for (ModuloCrud m : modulos) m.despuesEliminar(e, anterior);
    }

    // ---------- Conversión ----------

    private Object convertir(Campo c, String raw) {
        switch (c.tipo()) {
            case ENTERO -> {
                int n = Integer.parseInt(raw);
                if (n < 0) throw new ValidacionException("«" + c.etiqueta() + "» no puede ser negativo.");
                return n;
            }
            case DECIMAL -> {
                BigDecimal d = new BigDecimal(raw.replace(',', '.'));
                if (d.signum() < 0) throw new ValidacionException("«" + c.etiqueta() + "» no puede ser negativo.");
                return d;
            }
            case FECHA -> { return Date.valueOf(LocalDate.parse(raw)); }
            case FECHAHORA -> { return Timestamp.valueOf(LocalDateTime.parse(raw.replace(' ', 'T'))); }
            case FK -> { return Integer.valueOf(raw); }
            case LISTA -> {
                if (!c.opciones().contains(raw)) throw new ValidacionException("Elige una opción válida en «" + c.etiqueta() + "».");
                return raw;
            }
            default -> { return raw; }
        }
    }

    private String mostrar(Campo c, Object val, Map<String, Map<String, String>> etiquetas) {
        if (val == null) return "—";
        return switch (c.tipo()) {
            case FK -> etiquetas.getOrDefault(c.nombre(), Map.of()).getOrDefault(val.toString(), "#" + val);
            case FECHAHORA -> aLocal(val).format(F_VER);
            case DECIMAL -> Util.dec(val).toPlainString();
            default -> val.toString();
        };
    }

    private String paraFormulario(Campo c, Object val) {
        if (val == null) return "";
        return switch (c.tipo()) {
            case FECHAHORA -> aLocal(val).format(F_FORM);
            case DECIMAL -> Util.dec(val).toPlainString();
            default -> val.toString();
        };
    }

    private LocalDateTime aLocal(Object val) {
        if (val instanceof Timestamp t) return t.toLocalDateTime();
        if (val instanceof LocalDateTime l) return l;
        return LocalDateTime.parse(val.toString().replace(' ', 'T'));
    }

    private Campo campo(Entidad e, String nombre) {
        for (Campo c : e.campos()) if (c.nombre().equals(nombre)) return c;
        throw new IllegalArgumentException(nombre);
    }
}
