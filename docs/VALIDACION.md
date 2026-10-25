# Validación de la arquitectura inicial

Fecha: 25 de octubre de 2026.

## Entorno y comando

Windows, JDK 21.0.7 y Maven 3.9.16. Se utilizó `-Djava.version=21` porque el JDK disponible localmente es 21. El proyecto y el workflow de GitHub conservan Java 25; el resultado local no acredita una ejecución en ese JDK.

Comando equivalente desde `demo/`:

```powershell
.\mvnw.cmd -Djava.version=21 verify
```

Se usó un repositorio Maven temporal dentro del espacio de trabajo. Mockito se carga como agente explícito para evitar el bloqueo de la carga dinámica en este entorno Windows.

## TDD de inventario

1. Se escribió `InventarioTest` antes de crear el contrato DAO, el resumen y el servicio.
2. La primera compilación de pruebas falló con `cannot find symbol` para `InventarioDao`, `ResumenInventario` e `InventarioService`.
3. Se añadieron esas clases y se extrajo el SQL del controlador al DAO.
4. Se ejecutó la suite completa: **64 pruebas, 0 fallos, 0 errores, 0 omitidas**.

De esas pruebas, 10 son nuevas: 3 de inventario y 7 de seguridad web. Se corrigió además la prueba de arranque heredada para usar H2 en memoria, sin requerir MySQL. No se verificó la ejecución de consultas contra un servidor MySQL real.

## Cobertura y límites

- Conservación de artículos, totales y atributos de la plantilla de inventario.
- Autenticación y permisos de administrador/vendedor con la configuración real de Spring Security y endpoints de prueba.
- Rechazo de logout sin CSRF y aceptación con token.
- Codificación y comprobación de contraseña mediante BCrypt.
- Arranque del contexto con H2; no valida el esquema MySQL ni constituye una prueba funcional de todas las pantallas.

El workflow de GitHub ejecutará `verify` con Java 25 cuando la rama se publique. Su resultado debe consultarse en Actions; no se declara aprobado antes de observarlo.
