@echo off
rem Respaldo de la base telar_mx (RNF-10). Variables opcionales:
rem   DB_USER (root), DB_PASSWORD (vacia), DB_PORT (3307), MYSQLDUMP (ruta de XAMPP)
setlocal
if "%DB_USER%"=="" set DB_USER=root
if "%DB_PORT%"=="" set DB_PORT=3307
if "%MYSQLDUMP%"=="" set MYSQLDUMP=C:\xampp\mysql\bin\mysqldump.exe
if not "%DB_PASSWORD%"=="" set MYSQL_PWD=%DB_PASSWORD%

set DESTINO=%~dp0..\respaldos
if not exist "%DESTINO%" mkdir "%DESTINO%"
for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmm"') do set FECHA=%%i
set ARCHIVO=%DESTINO%\telar_mx_%FECHA%.sql

"%MYSQLDUMP%" -h 127.0.0.1 -P %DB_PORT% -u %DB_USER% --routines --single-transaction telar_mx > "%ARCHIVO%"
if errorlevel 1 (
    echo ERROR: no se pudo crear el respaldo. Revisa que MySQL este encendido y las variables DB_USER y DB_PASSWORD.
    del "%ARCHIVO%" 2>nul
    exit /b 1
)
echo Respaldo creado: %ARCHIVO%

rem Se conservan solo los respaldos de los ultimos 30 dias
forfiles /p "%DESTINO%" /m telar_mx_*.sql /d -30 /c "cmd /c del @path" 2>nul
exit /b 0
