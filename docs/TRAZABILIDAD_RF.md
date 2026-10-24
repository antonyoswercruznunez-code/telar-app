# Trazabilidad: requerimientos (SRS 12.3) → código

| RF | Pantalla / ruta | Clases principales | Pruebas |
|---|---|---|---|
| RF-01 | `/clientes/nuevo` | `ClienteController`, `ClienteService`, `ClienteRepository` | `ClienteServiceTest` |
| RF-02 | `/clientes?q=` | `ClienteRepository.buscar` | — |
| RF-03 | `/clientes/{id}/editar`, `/desactivar`, `/activar` | `ClienteService`, columna `cliente.activo` (migración 01) | `ClienteServiceTest` |
| RF-04 | `/clientes/{id}/historial` | `ClienteService.historial`, `HistorialCliente` | `ClienteServiceTest` |
| RF-05, RF-06 | `/productos/nuevo`, `/productos/{id}/editar` | `ProductoController`, `ProductoService` | `ProductoValidadorTest` |
| RF-07 | Validación al crear/editar producto | `ProductoValidador`, `ProductoService.validarNuevo` | `ProductoValidadorTest`, `ProductoServiceTest` |
| RF-08 | `/productos/{id}/desactivar`, `/activar` | `ProductoService`, columna `producto.activo` (migración 02) | `ProductoServiceTest` |
| RF-09, RF-10 | `/gestion/pedido`, `/gestion/detalle_pedido`, `/gestion/medida_personalizada` | `PedidosModulo` (CRUD genérico) | — |
| RF-11 | `/pedidos/{id}/seguimiento` (avanzar / cambiar estado) | `EstadoPedido`, `PedidoSeguimientoService` (migración 03) | `EstadoPedidoTest`, `PedidoSeguimientoServiceTest` |
| RF-12 | `/pedidos/seguimiento` (filtros por estado y fechas) | `SeguimientoController`, `PedidoSeguimientoRepository` | `PedidoSeguimientoServiceTest` |
| RF-13 a RF-15 | `/gestion/venta`, `/gestion/detalle_venta`, `/gestion/comprobante` | `VentasModulo`, `StockService` | `StockServiceTest` |
| RF-16 | `/gestion/venta` (filtros), `/reportes` | `VentasModulo`, `ReporteService` | — (verificar filtro por producto) |
| RF-17 | Al registrar el detalle de venta | `StockService.descontar` | `StockServiceTest` |
| RF-18, RF-19 | `/inventario` (stock mínimo marcado y banner de alerta) | `InventarioController` | — |
| RF-20 a RF-22 | `/reportes` → ventas, pedidos, stock | `ReporteService`, `ReporteRepository` | `ReporteServiceTest` |
| RF-23 | `/reportes` (formato PDF) | `ReportePdfService`, `FormatoReporte` | `ReporteServiceTest`, `FormatoReporteTest` |
| RF-24 | `/login` | `SeguridadConfig`, `UsuarioDetailsService` | — |
| RF-25 | Menú y URLs por rol (Administrador / Personal de ventas) | `Rol`, `Permisos`, `SeguridadConfig`, `MenuApp` | `RolTest`, `MenuAppTest` |

| Extra | Pantalla / ruta | Clases principales | Pruebas |
|---|---|---|---|
| Dashboard (indicadores, gráficos, alertas; se actualiza solo cada minuto) | `/dashboard`, datos en `/dashboard/datos` | `DashboardController`, `DashboardService`, `DashboardRepository`, `static/js/dashboard.js` | `DashboardServiceTest` |
| Logo e identidad visual | Menú, login y pestaña del navegador | `static/img/logo-telar-mx.svg` | — |
| Tablas ordenables y avisos que se desvanecen | Todas las listas | `static/js/app.js` | — |

| RNF | Cómo se cumple |
|---|---|
| RNF-03 | Contraseñas con BCrypt; acceso por roles; clave de BD en variable de entorno |
| RNF-06 | Diseño responsivo (CSS con media queries) |
| RNF-07 | Capas controller → service → repository (DAO) |
| RNF-09 | `ClienteValidador`, `ProductoValidador`, consultas con parámetros (?) |
| RNF-10 | `demo/scripts/respaldo_bd.bat` / `.sh` (ver `RESPALDO.md`) |
