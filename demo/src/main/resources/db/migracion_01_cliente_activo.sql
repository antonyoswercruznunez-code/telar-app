-- Migración 01 (RF-03): permite desactivar clientes sin borrarlos.
-- Ejecutar UNA vez sobre una base telar_mx ya creada. Si creas la base desde telar_mx.sql, no hace falta.
ALTER TABLE `cliente` ADD COLUMN `activo` tinyint(1) NOT NULL DEFAULT 1 AFTER `Numero_Documento`;
