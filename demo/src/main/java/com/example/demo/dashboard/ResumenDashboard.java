package com.example.demo.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Todo lo que muestra el dashboard. Se entrega como JSON en /dashboard/datos. */
public record ResumenDashboard(
        BigDecimal ventasHoy, int cantidadVentasHoy, BigDecimal ventasMes, int cantidadVentasMes,
        BigDecimal saldoPorCobrar, int pedidosActivos, int pedidosAtrasados, int alertasStock, int clientesActivos,
        List<PuntoGrafico> ventasUltimosDias, List<PuntoGrafico> pedidosPorEstado,
        List<Map<String, Object>> pedidosPorEntregar, List<Map<String, Object>> stockBajo,
        List<Map<String, Object>> ultimasVentas) { }
