# Telar MX

Aplicación Spring Boot, Thymeleaf y MySQL. Arquitectura organizada por módulos de negocio.

## Ejecutar

Requisitos: JDK 25 y MySQL. Importar `demo/src/main/resources/db/telar_mx.sql` y revisar las migraciones de `db/` antes de aplicarlas a una base existente. Los datos de demostración no deben utilizarse en producción.

Configurar `DB_URL`, `DB_USER` y `DB_PASSWORD` en el entorno con una cuenta de MySQL de privilegios mínimos. Los valores actuales por defecto son para desarrollo local.

Desde `demo/`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

En Linux/macOS usar `bash mvnw` en lugar de `mvnw.cmd`.

## Arquitectura y evidencias

Ver [arquitectura](docs/ARQUITECTURA_INICIAL.md), [evidencias](docs/EVIDENCIAS_RUBRICA.md) y [requerimientos](docs/TRAZABILIDAD_RF.md).

La referencia de separación por capas es inventario:

`InventarioController → InventarioService → InventarioDao ← InventarioJdbcDao → MySQL`

La vista continúa en `templates/catalogo/inventario.html`.

## GitHub

Trabajar en ramas `feature/...`, ejecutar las pruebas y abrir un Pull Request a `main`. El workflow `.github/workflows/verificar.yml` ejecuta `verify` con Java 25 en cada Pull Request y en pushes a `main` o `feature/**`. La protección de ramas debe configurarse en GitHub para exigir el check antes de fusionar.

No subir contraseñas, archivos `.env`, respaldos ni logs. No se debe afirmar que todo el proyecto se desarrolló con TDD únicamente porque existen pruebas.
