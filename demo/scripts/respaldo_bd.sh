#!/usr/bin/env bash
# Respaldo de la base telar_mx (RNF-10). Variables opcionales: DB_USER, DB_PASSWORD, DB_PORT
set -euo pipefail
DB_USER="${DB_USER:-root}"
DB_PORT="${DB_PORT:-3307}"
[ -n "${DB_PASSWORD:-}" ] && export MYSQL_PWD="$DB_PASSWORD"

DESTINO="$(dirname "$0")/../respaldos"
mkdir -p "$DESTINO"
ARCHIVO="$DESTINO/telar_mx_$(date +%Y%m%d_%H%M).sql"

mysqldump -h 127.0.0.1 -P "$DB_PORT" -u "$DB_USER" --routines --single-transaction telar_mx > "$ARCHIVO"
echo "Respaldo creado: $ARCHIVO"

# Se conservan solo los respaldos de los últimos 30 días
find "$DESTINO" -name 'telar_mx_*.sql' -mtime +30 -delete
