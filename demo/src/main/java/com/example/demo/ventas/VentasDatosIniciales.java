package com.example.demo.ventas;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Carga los métodos de pago básicos si la tabla está vacía. */
@Component
public class VentasDatosIniciales implements ApplicationRunner {
    private final JdbcTemplate jdbc;

    public VentasDatosIniciales(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM metodo_pago", Integer.class);
            if (total != null && total == 0) {
                for (String nombre : new String[]{"Efectivo", "Yape", "Plin", "Transferencia", "Tarjeta"}) {
                    jdbc.update("INSERT INTO metodo_pago (nombre) VALUES (?)", nombre);
                }
            }
        } catch (DataAccessException ex) {
            // La tabla aún no existe: ejecuta primero db/telar_mx.sql.
        }
    }
}
