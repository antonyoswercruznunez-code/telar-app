package com.example.demo.reportes;

import com.example.demo.pedidos.EstadoPedido;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Orquesta consulta (DAO) + exportación (Excel o PDF) y valida los parámetros del reporte. */
@Service
public class ReporteService {
    static final int MAXIMO_DIAS = 366;
    private static final Logger log = LoggerFactory.getLogger(ReporteService.class);

    private final ReporteRepository repository;
    private final ReporteExcelService excel;
    private final ReportePdfService pdf;

    public ReporteService(ReporteRepository repository, ReporteExcelService excel, ReportePdfService pdf) {
        this.repository = repository;
        this.excel = excel;
        this.pdf = pdf;
    }

    public byte[] inventario(FormatoReporte formato) {
        TablaReporte tabla = repository.inventario();
        log.info("Reporte de stock disponible ({}): {} filas", formato, tabla.filas().size());
        return exportar("Inventario", tabla, formato);
    }

    public byte[] ventas(LocalDate desde, LocalDate hasta, FormatoReporte formato) {
        Preconditions.checkArgument(desde != null && hasta != null, "Indica la fecha inicial y la final.");
        Preconditions.checkArgument(!hasta.isBefore(desde), "La fecha final no puede ser anterior a la inicial.");
        Preconditions.checkArgument(ChronoUnit.DAYS.between(desde, hasta) <= MAXIMO_DIAS,
                "El rango no puede superar %s días.", MAXIMO_DIAS);
        TablaReporte tabla = repository.ventas(desde, hasta);
        log.info("Reporte de ventas {} a {} ({}): {} filas", desde, hasta, formato, tabla.filas().size());
        return exportar("Ventas", tabla, formato);
    }

    /** @param estado vacío o null = todos los estados */
    public byte[] pedidos(String estado, FormatoReporte formato) {
        Preconditions.checkArgument(StringUtils.isBlank(estado) || EstadoPedido.de(estado).isPresent(),
                "Estado de pedido no válido.");
        TablaReporte tabla = repository.pedidos(estado);
        log.info("Reporte de pedidos, estado «{}» ({}): {} filas", estado, formato, tabla.filas().size());
        return exportar("Pedidos", tabla, formato);
    }

    public byte[] insumosBajos(FormatoReporte formato) {
        TablaReporte tabla = repository.insumosBajos();
        log.info("Reporte de insumos con stock bajo ({}): {} filas", formato, tabla.filas().size());
        return exportar("Insumos bajos", tabla, formato);
    }

    private byte[] exportar(String titulo, TablaReporte tabla, FormatoReporte formato) {
        return formato == FormatoReporte.PDF ? pdf.generar(titulo, tabla) : excel.generar(titulo, tabla);
    }
}
