package com.example.demo.comun;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registro de todas las entidades declaradas por los módulos. */
@Component
public class Entidades {
    private final Map<String, Entidad> mapa = new LinkedHashMap<>();

    public Entidades(List<ModuloCrud> modulos) {
        for (ModuloCrud m : modulos) {
            for (Entidad e : m.entidades()) mapa.put(e.clave(), e);
        }
    }

    public Entidad requerir(String clave) {
        Entidad e = mapa.get(clave);
        if (e == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return e;
    }

    public String titulo(String clave) { return requerir(clave).titulo(); }
}
