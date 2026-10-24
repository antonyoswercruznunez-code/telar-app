-- Migración 03 (RF-11): estados del pedido según el SRS: Recibido, En proceso, Listo, Entregado (y Cancelado).
-- Ejecutar UNA vez sobre una base telar_mx ya creada.
UPDATE `pedido` SET `estado` = 'Recibido'   WHERE `estado` = 'Pendiente';
UPDATE `pedido` SET `estado` = 'En proceso' WHERE `estado` = 'En producción';
