package com.example.demo.catalogo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventarioService {
    private final InventarioDao dao;

    public InventarioService(InventarioDao dao) { this.dao = dao; }

    @Transactional(readOnly = true)
    public ResumenInventario consultar() { return dao.consultar(); }
}
