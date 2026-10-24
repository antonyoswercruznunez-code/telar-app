-- =====================================================================
-- 15.3. Script SQL de creación de tablas, relaciones, restricciones e índices
-- Base de datos: telar_mx
-- Ejecutar en el orden indicado:
--   15.3.1 Creación de tablas
--   15.3.2 Relaciones (claves foráneas)
--   15.3.3 Índices y restricciones de optimización
-- Dentro de cada parte, el orden sigue los módulos del sistema:
--   1. Seguridad y usuarios
--   2. Clientes y pedidos
--   3. Catálogo de productos e inventario
--   4. Ventas, pagos y caja
--   5. Producción
--   6. Compras e insumos
-- =====================================================================

-- =====================================================================
-- 15.3.1 CREACIÓN DE TABLAS
-- =====================================================================
CREATE DATABASE IF NOT EXISTS `telar_mx` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `telar_mx`;

-- ---------------------------------------------------------------------
-- MÓDULO 1: Seguridad y usuarios
-- ---------------------------------------------------------------------
CREATE TABLE `usuario` (
  `id_usuario` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `correo` varchar(100) NOT NULL,
  `contrasena` varchar(255) NOT NULL,
  `rol` varchar(20) NOT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `correo` (`correo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ---------------------------------------------------------------------
-- MÓDULO 2: Clientes y pedidos
-- ---------------------------------------------------------------------
CREATE TABLE `cliente` (
  `id_cliente` int(11) NOT NULL AUTO_INCREMENT,
  `tipo_cliente` varchar(20) NOT NULL DEFAULT 'Persona',
  `razon_social` varchar(150) DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `correo` varchar(100) DEFAULT NULL,
  `persona_contacto` varchar(100) DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `Tipo_Documento` varchar(20) NOT NULL,
  `Numero_Documento` varchar(20) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_cliente`),
  UNIQUE KEY `Numero_Documento` (`Numero_Documento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `pedido` (
  `id_pedido` int(11) NOT NULL AUTO_INCREMENT,
  `id_cliente` int(11) NOT NULL,
  `id_usuario` int(11) DEFAULT NULL,
  `fecha_pedido` datetime NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `estado` varchar(20) NOT NULL,
  `fecha_entrega` date DEFAULT NULL,
  PRIMARY KEY (`id_pedido`),
  KEY `fk_pedido_cliente_rel` (`id_cliente`),
  KEY `fk_pedido_usuario_rel` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `detalle_pedido` (
  `id_detalle` int(11) NOT NULL AUTO_INCREMENT,
  `id_pedido` int(11) NOT NULL,
  `id_detalle_producto` int(11) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_detalle`),
  KEY `fk_detalle_pedido_pedido` (`id_pedido`),
  KEY `fk_detalle_pedido_producto` (`id_detalle_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `medida_personalizada` (
  `id_medida` int(11) NOT NULL AUTO_INCREMENT,
  `id_detalle_pedido` int(11) NOT NULL,
  `pecho` decimal(5,2) DEFAULT NULL,
  `cintura` decimal(5,2) DEFAULT NULL,
  `cadera` decimal(5,2) DEFAULT NULL,
  `largo_total` decimal(5,2) DEFAULT NULL,
  `observaciones_corte` text DEFAULT NULL,
  PRIMARY KEY (`id_medida`),
  KEY `fk_medida_det_pedido` (`id_detalle_pedido`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ---------------------------------------------------------------------
-- MÓDULO 3: Catálogo de productos e inventario
-- ---------------------------------------------------------------------
CREATE TABLE `producto` (
  `id_producto` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `precio` decimal(10,2) NOT NULL,
  `precio_mayorista` decimal(10,2) DEFAULT NULL,
  `cantidad_minima_mayorista` int(11) DEFAULT 12,
  `categoria` varchar(50) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `detalle_producto` (
  `id_detalle_producto` int(11) NOT NULL AUTO_INCREMENT,
  `id_producto` int(11) NOT NULL,
  `color` varchar(30) NOT NULL,
  `talla` varchar(10) NOT NULL,
  `material` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_detalle_producto`),
  KEY `fk_detalle_producto_producto` (`id_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `stock` (
  `id_stock` int(11) NOT NULL AUTO_INCREMENT,
  `id_detalle_producto` int(11) NOT NULL,
  `cantidad_disponible` int(11) NOT NULL DEFAULT 0,
  `cantidad_minima` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_stock`),
  KEY `fk_stock_detalle_producto_rel` (`id_detalle_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ---------------------------------------------------------------------
-- MÓDULO 4: Ventas, pagos y caja
-- ---------------------------------------------------------------------
CREATE TABLE `venta` (
  `id_venta` int(11) NOT NULL AUTO_INCREMENT,
  `id_pedido` int(11) DEFAULT NULL,
  `id_cliente` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `tipo_venta` varchar(30) NOT NULL DEFAULT 'Minorista',
  `fecha_venta` datetime NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `monto_adelanto` decimal(10,2) NOT NULL DEFAULT 0.00,
  `saldo_pendiente` decimal(10,2) NOT NULL DEFAULT 0.00,
  `estado_pago` varchar(20) NOT NULL DEFAULT 'Pagado',
  PRIMARY KEY (`id_venta`),
  KEY `fk_venta_cliente_rel` (`id_cliente`),
  KEY `fk_venta_pedido_rel` (`id_pedido`),
  KEY `fk_venta_usuario_rel` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `detalle_venta` (
  `id_detalle_venta` int(11) NOT NULL AUTO_INCREMENT,
  `id_venta` int(11) NOT NULL,
  `id_detalle_producto` int(11) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_detalle_venta`),
  KEY `fk_detalle_venta_venta` (`id_venta`),
  KEY `fk_detalle_venta_producto` (`id_detalle_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `metodo_pago` (
  `id_metodo_pago` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  PRIMARY KEY (`id_metodo_pago`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `pago_venta` (
  `id_pago` int(11) NOT NULL AUTO_INCREMENT,
  `id_venta` int(11) NOT NULL,
  `id_metodo_pago` int(11) NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `referencia` varchar(100) DEFAULT NULL,
  `fecha_pago` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_pago`),
  KEY `fk_pago_venta` (`id_venta`),
  KEY `fk_pago_metodo` (`id_metodo_pago`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `comprobante` (
  `id_comprobante` int(11) NOT NULL AUTO_INCREMENT,
  `id_venta` int(11) NOT NULL,
  `tipo_comprobante` varchar(20) NOT NULL,
  `serie` varchar(10) NOT NULL,
  `numero` int(11) NOT NULL,
  `fecha_emision` datetime NOT NULL DEFAULT current_timestamp(),
  `estado` varchar(20) NOT NULL DEFAULT 'Emitido',
  PRIMARY KEY (`id_comprobante`),
  KEY `fk_comprobante_venta` (`id_venta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `cierre_caja` (
  `id_cierre` int(11) NOT NULL AUTO_INCREMENT,
  `id_usuario` int(11) NOT NULL,
  `fecha_apertura` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_cierre` datetime DEFAULT NULL,
  `monto_inicial` decimal(10,2) NOT NULL DEFAULT 0.00,
  `monto_final_efectivo` decimal(10,2) DEFAULT NULL,
  `total_ventas_calculado` decimal(10,2) DEFAULT 0.00,
  `estado` varchar(20) NOT NULL DEFAULT 'Abierta',
  PRIMARY KEY (`id_cierre`),
  KEY `fk_cierre_usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `reporte` (
  `id_reporte` int(11) NOT NULL AUTO_INCREMENT,
  `id_usuario` int(11) NOT NULL,
  `tipo` varchar(30) NOT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date NOT NULL,
  `total_vendido` decimal(10,2) DEFAULT 0.00,
  `id_producto_mas_vendido` int(11) DEFAULT NULL,
  `id_producto_faltante` int(11) DEFAULT NULL,
  `fecha_generacion` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id_reporte`),
  KEY `id_producto_mas_vendido` (`id_producto_mas_vendido`),
  KEY `id_producto_faltante` (`id_producto_faltante`),
  KEY `fk_reporte_usuario_rel` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ---------------------------------------------------------------------
-- MÓDULO 5: Producción
-- ---------------------------------------------------------------------
CREATE TABLE `orden_produccion` (
  `id_orden_produccion` int(11) NOT NULL AUTO_INCREMENT,
  `id_pedido` int(11) DEFAULT NULL,
  `id_detalle_producto` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `cantidad_a_producir` int(11) NOT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin_estimada` date DEFAULT NULL,
  `estado_fase` varchar(30) NOT NULL DEFAULT 'En Corte',
  PRIMARY KEY (`id_orden_produccion`),
  KEY `fk_op_pedido_rel` (`id_pedido`),
  KEY `fk_op_producto_rel` (`id_detalle_producto`),
  KEY `fk_op_usuario_rel` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `detalle_orden_produccion` (
  `id_detalle_op` int(11) NOT NULL AUTO_INCREMENT,
  `id_orden_produccion` int(11) NOT NULL,
  `id_insumo` int(11) NOT NULL,
  `cantidad_estimada` decimal(10,2) NOT NULL,
  `cantidad_real_usada` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id_detalle_op`),
  KEY `fk_det_op_cabecera` (`id_orden_produccion`),
  KEY `fk_det_op_insumo` (`id_insumo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `ficha_tecnica` (
  `id_ficha` int(11) NOT NULL AUTO_INCREMENT,
  `id_detalle_producto` int(11) NOT NULL,
  `id_insumo` int(11) NOT NULL,
  `cantidad_consumo` decimal(10,2) NOT NULL,
  `porcentaje_merma` decimal(5,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (`id_ficha`),
  KEY `fk_ficha_producto` (`id_detalle_producto`),
  KEY `fk_ficha_insumo` (`id_insumo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ---------------------------------------------------------------------
-- MÓDULO 6: Compras e insumos
-- ---------------------------------------------------------------------
CREATE TABLE `proveedor` (
  `id_proveedor` int(11) NOT NULL AUTO_INCREMENT,
  `razon_social` varchar(150) NOT NULL,
  `ruc_rfc` varchar(20) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `correo` varchar(100) DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id_proveedor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `insumo` (
  `id_insumo` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `unidad_medida` varchar(20) NOT NULL,
  `cantidad_disponible` decimal(10,2) NOT NULL DEFAULT 0.00,
  `cantidad_minima` decimal(10,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (`id_insumo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `compra` (
  `id_compra` int(11) NOT NULL AUTO_INCREMENT,
  `id_proveedor` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `fecha_compra` datetime NOT NULL DEFAULT current_timestamp(),
  `numero_comprobante` varchar(50) DEFAULT NULL,
  `total` decimal(10,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (`id_compra`),
  KEY `fk_compra_proveedor` (`id_proveedor`),
  KEY `fk_compra_usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `detalle_compra` (
  `id_detalle_compra` int(11) NOT NULL AUTO_INCREMENT,
  `id_compra` int(11) NOT NULL,
  `id_insumo` int(11) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `precio_compra_unitario` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_detalle_compra`),
  KEY `fk_detalle_compra_compra` (`id_compra`),
  KEY `fk_det_compra_insumo` (`id_insumo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `conversion_unidad` (
  `id_conversion` int(11) NOT NULL AUTO_INCREMENT,
  `id_insumo` int(11) NOT NULL,
  `unidad_compra` varchar(20) NOT NULL,
  `unidad_consumo` varchar(20) NOT NULL,
  `factor_conversion` decimal(10,4) NOT NULL,
  PRIMARY KEY (`id_conversion`),
  KEY `fk_conversion_insumo` (`id_insumo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- =====================================================================
-- 15.3.2 RELACIONES (CLAVES FORÁNEAS)
-- =====================================================================

-- ---------------------------------------------------------------------
-- MÓDULO 2: Clientes y pedidos
-- ---------------------------------------------------------------------
ALTER TABLE `pedido`
  ADD CONSTRAINT `fk_pedido_cliente_rel` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_pedido_usuario_rel` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `detalle_pedido`
  ADD CONSTRAINT `fk_detalle_pedido_pedido` FOREIGN KEY (`id_pedido`) REFERENCES `pedido` (`id_pedido`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_detalle_pedido_producto` FOREIGN KEY (`id_detalle_producto`) REFERENCES `detalle_producto` (`id_detalle_producto`) ON UPDATE CASCADE;

-- (Añadida: faltaba en el script original)
ALTER TABLE `medida_personalizada`
  ADD CONSTRAINT `fk_medida_det_pedido` FOREIGN KEY (`id_detalle_pedido`) REFERENCES `detalle_pedido` (`id_detalle`) ON DELETE CASCADE ON UPDATE CASCADE;

-- ---------------------------------------------------------------------
-- MÓDULO 3: Catálogo de productos e inventario
-- ---------------------------------------------------------------------
ALTER TABLE `detalle_producto`
  ADD CONSTRAINT `fk_detalle_producto_producto` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id_producto`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `stock`
  ADD CONSTRAINT `fk_stock_detalle_producto_rel` FOREIGN KEY (`id_detalle_producto`) REFERENCES `detalle_producto` (`id_detalle_producto`) ON DELETE CASCADE ON UPDATE CASCADE;

-- ---------------------------------------------------------------------
-- MÓDULO 4: Ventas, pagos y caja
-- ---------------------------------------------------------------------
ALTER TABLE `venta`
  ADD CONSTRAINT `fk_venta_cliente_rel` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_venta_pedido_rel` FOREIGN KEY (`id_pedido`) REFERENCES `pedido` (`id_pedido`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_venta_usuario_rel` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON UPDATE CASCADE;

ALTER TABLE `detalle_venta`
  ADD CONSTRAINT `fk_detalle_venta_venta` FOREIGN KEY (`id_venta`) REFERENCES `venta` (`id_venta`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_detalle_venta_producto` FOREIGN KEY (`id_detalle_producto`) REFERENCES `detalle_producto` (`id_detalle_producto`) ON UPDATE CASCADE;

ALTER TABLE `pago_venta`
  ADD CONSTRAINT `fk_pago_venta` FOREIGN KEY (`id_venta`) REFERENCES `venta` (`id_venta`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_pago_metodo` FOREIGN KEY (`id_metodo_pago`) REFERENCES `metodo_pago` (`id_metodo_pago`) ON UPDATE CASCADE;

ALTER TABLE `comprobante`
  ADD CONSTRAINT `fk_comprobante_venta` FOREIGN KEY (`id_venta`) REFERENCES `venta` (`id_venta`) ON UPDATE CASCADE;

ALTER TABLE `cierre_caja`
  ADD CONSTRAINT `fk_cierre_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON UPDATE CASCADE;

ALTER TABLE `reporte`
  ADD CONSTRAINT `fk_reporte_prod_vendido` FOREIGN KEY (`id_producto_mas_vendido`) REFERENCES `producto` (`id_producto`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_reporte_prod_faltante` FOREIGN KEY (`id_producto_faltante`) REFERENCES `producto` (`id_producto`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_reporte_usuario_rel` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON UPDATE CASCADE;

-- ---------------------------------------------------------------------
-- MÓDULO 5: Producción
-- ---------------------------------------------------------------------
-- (Se añadió fk_op_producto_rel: faltaba en el script original)
ALTER TABLE `orden_produccion`
  ADD CONSTRAINT `fk_op_pedido_rel` FOREIGN KEY (`id_pedido`) REFERENCES `pedido` (`id_pedido`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_op_producto_rel` FOREIGN KEY (`id_detalle_producto`) REFERENCES `detalle_producto` (`id_detalle_producto`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_op_usuario_rel` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON UPDATE CASCADE;

ALTER TABLE `detalle_orden_produccion`
  ADD CONSTRAINT `fk_det_op_cabecera` FOREIGN KEY (`id_orden_produccion`) REFERENCES `orden_produccion` (`id_orden_produccion`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_det_op_insumo` FOREIGN KEY (`id_insumo`) REFERENCES `insumo` (`id_insumo`) ON UPDATE CASCADE;

ALTER TABLE `ficha_tecnica`
  ADD CONSTRAINT `fk_ficha_producto` FOREIGN KEY (`id_detalle_producto`) REFERENCES `detalle_producto` (`id_detalle_producto`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_ficha_insumo` FOREIGN KEY (`id_insumo`) REFERENCES `insumo` (`id_insumo`) ON UPDATE CASCADE;

-- ---------------------------------------------------------------------
-- MÓDULO 6: Compras e insumos
-- ---------------------------------------------------------------------
ALTER TABLE `compra`
  ADD CONSTRAINT `fk_compra_proveedor` FOREIGN KEY (`id_proveedor`) REFERENCES `proveedor` (`id_proveedor`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_compra_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON UPDATE CASCADE;

ALTER TABLE `detalle_compra`
  ADD CONSTRAINT `fk_detalle_compra_compra` FOREIGN KEY (`id_compra`) REFERENCES `compra` (`id_compra`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_det_compra_insumo` FOREIGN KEY (`id_insumo`) REFERENCES `insumo` (`id_insumo`) ON UPDATE CASCADE;

ALTER TABLE `conversion_unidad`
  ADD CONSTRAINT `fk_conversion_insumo` FOREIGN KEY (`id_insumo`) REFERENCES `insumo` (`id_insumo`) ON DELETE CASCADE ON UPDATE CASCADE;


-- =====================================================================
-- 15.3.3 ÍNDICES Y RESTRICCIONES DE OPTIMIZACIÓN
-- Los índices de clave foránea y las claves únicas (usuario.correo,
-- cliente.Numero_Documento) ya están definidos en 15.3.1.
-- =====================================================================
