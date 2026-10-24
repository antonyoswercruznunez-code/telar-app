# Respaldo y restauración de la base de datos (RNF-10)

## Hacer un respaldo
Con MySQL encendido (XAMPP), desde `demo/`:

    scripts\respaldo_bd.bat          (Windows)
    ./scripts/respaldo_bd.sh         (Linux / macOS)

Se crea `demo/respaldos/telar_mx_AAAAMMDD_HHMM.sql` y se conservan los últimos 30 días.
La clave se toma de la variable de entorno `DB_PASSWORD` (no se escribe en el script).

## Programarlo cada día (Windows)
    schtasks /create /tn "Respaldo Telar MX" /tr "C:\ruta\telar-app\demo\scripts\respaldo_bd.bat" /sc daily /st 22:00

## Restaurar
    mysql -h 127.0.0.1 -P 3307 -u root telar_mx < respaldos\telar_mx_AAAAMMDD_HHMM.sql

La carpeta `respaldos/` está en `.gitignore`: los respaldos tienen datos de clientes y no deben subirse a GitHub.
