# Dónde está cada criterio de la rúbrica

Rutas relativas a `demo/src/main/java/com/example/demo/` (código) y `demo/src/test/java/com/example/demo/` (pruebas).

## 1. Arquitectura inicial: MVC, TDD, DAO, SOLID y seguridad (3 pts)

| Principio | Evidencia |
|---|---|
| **MVC** | Modelo: `ClienteForm`, `ProductoForm`, `HistorialCliente`, `ResumenDashboard`. Vista: `demo/src/main/resources/templates/` (Thymeleaf, una carpeta por módulo). Controlador: `ClienteController`, `ProductoController`, `SeguimientoController`, `ReporteController`, `DashboardController`, `UsuarioController`, `CrudController` |
| **DAO** | Una clase `*Repository` por módulo con todo el SQL (JdbcTemplate): `ClienteRepository`, `ProductoRepository`, `PedidoSeguimientoRepository`, `ReporteRepository`, `DashboardRepository`, `UsuarioRepository`. Los controllers nunca tocan SQL |
| **TDD** | 53 pruebas unitarias en 13 clases de `src/test` (JUnit 5 + Mockito). No necesitan base de datos. Ver `StockServiceTest`, `ClienteServiceTest`, `ProductoServiceTest`, `PedidoSeguimientoServiceTest`, `ReporteServiceTest`, `DashboardServiceTest` |
| **SOLID** | **S**: validar (`ClienteValidador`, `ProductoValidador`), consultar (`*Repository`), orquestar (`*Service`) y exportar (`ReporteExcelService`, `ReportePdfService`) están separados. **O**: `FormatoReporte` y `EstadoPedido` (enums) permiten añadir formatos o estados sin tocar los controllers. **D**: todo se inyecta por constructor (`DashboardService` recibe un `Clock`, por eso se prueba con fecha fija). **Una sola fuente de verdad**: `seguridad/Permisos.java` |
| **Seguridad** | Contraseñas con BCrypt (`SeguridadConfig`); 2 roles (`Rol`, `Permisos`) con bloqueo de URLs por rol; CSRF activo en todos los formularios; consultas con parámetros `?` (sin inyección SQL); clave de BD por variable de entorno (`application.properties`); salidas escapadas en Thymeleaf y `textContent` en el JS del dashboard; `.gitignore` excluye secretos y respaldos |

## 2. Librerías de apoyo (2 pts)

| Librería | Dónde se usa | Para qué |
|---|---|---|
| **Apache POI** | `reportes/ReporteExcelService` | Exportar ventas, pedidos, stock e insumos a Excel (.xlsx) |
| **Google Guava** | `Preconditions` en `ReporteService`, `ReporteExcelService`, `ReportePdfService`, `PedidoSeguimientoService`, `StockService` | Validar argumentos con mensajes claros |
| **Apache Commons Lang** | `StringUtils` en `ClienteValidador`, `ProductoValidador`, `ProductoService`, `ReporteService`, `ReporteExcelService`, `FormatoReporte`, `MenuApp` | Validar y limpiar texto |
| **Logback (SLF4J)** | Configuración: `src/main/resources/logback-spring.xml` (consola + archivo diario en `logs/`). Uso: `ClienteService`, `ProductoService`, `StockService`, `PedidoSeguimientoService`, `ReporteService`, `ReporteController`, `UsuarioController` | Registrar quién hizo qué (sin contraseñas) |
| OpenPDF (extra) | `reportes/ReportePdfService` | Exportar reportes a PDF (RF-23) |

Seguridad de la información: los logs nunca incluyen contraseñas; `respaldos/` y `logs/` no se suben a Git.

## 3. Git y GitHub (3 pts)
Pendiente de ejecutar por el estudiante: crear el repositorio, commits por funcionalidad, ramas `feature/...` con Pull Requests y tags `v0.3-avance-30` / `v0.4-entrega-40`. El `.gitignore` ya está listo en la raíz.

## 4. Interfaces UI/UX al 100% del alcance (6 pts)
Pantallas por módulo en `demo/src/main/resources/templates/`: `dashboard/`, `clientes/` (lista, formulario, historial), `catalogo/` (productos, variantes, inventario), `pedidos/` (seguimiento y detalle), `comun/` (ventas, pagos, compras, producción: pantallas genéricas), `reportes/`, `usuarios/`, `login.html`. Estilos en `static/css/`, comportamiento en `static/js/`. Tabla RF → pantalla en `TRAZABILIDAD_RF.md`.

## 5. Solución completa, coherente, buenas prácticas y autoría (4 pts)
- **Completa**: `TRAZABILIDAD_RF.md` (los 25 RF y los RNF con su pantalla, clase y prueba).
- **Coherente**: estados de pedido y roles alineados con el SRS; scripts de migración en `db/` documentan los cambios de esquema.
- **Buenas prácticas**: capas, validadores, pruebas, logs, `.gitignore`, respaldo (`RESPALDO.md`).
- **Autoría**: el estudiante debe poder explicar el flujo de una venta, por qué cada patrón y qué hace cada librería.
