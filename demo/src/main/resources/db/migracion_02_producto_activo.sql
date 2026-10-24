-- Migración 02 (RF-08): permite desactivar productos sin borrarlos.
-- Ejecutar UNA vez sobre una base telar_mx ya creada. Si creas la base desde telar_mx.sql, no hace falta.
ALTER TABLE `producto` ADD COLUMN `activo` tinyint(1) NOT NULL DEFAULT 1 AFTER `categoria`;
