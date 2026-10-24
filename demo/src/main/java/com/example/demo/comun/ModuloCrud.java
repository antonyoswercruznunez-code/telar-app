package com.example.demo.comun;

import java.util.List;
import java.util.Map;

/**
 * Un módulo del sistema: declara sus tablas y, si hace falta, reglas de negocio.
 * Todos los módulos reciben los eventos de todas las entidades; cada uno atiende solo las suyas.
 */
public interface ModuloCrud {
    List<Entidad> entidades();

    /** Antes de guardar: validar y completar valores calculados en {@code valores}. {@code anterior} es null al crear. */
    default void antes(Entidad e, Map<String, Object> valores, Map<String, Object> anterior) { }

    /** Después de guardar (misma transacción): efectos sobre otras tablas. */
    default void despues(Entidad e, int id, Map<String, Object> valores, Map<String, Object> anterior) { }

    /** Antes de eliminar (aún existen los registros hijos en cascada). */
    default void antesEliminar(Entidad e, Map<String, Object> anterior) { }

    default void despuesEliminar(Entidad e, Map<String, Object> anterior) { }
}
