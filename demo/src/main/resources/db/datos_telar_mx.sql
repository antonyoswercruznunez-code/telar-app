-- ============================================================
-- Telar MX - datos de ejemplo para la base `telar_mx`
-- ============================================================
-- USUARIOS DE PRUEBA (contrasenas cifradas con BCrypt, listas para la app)
--   admin@telar.com   / Admin12345    -> ADMIN
--   lucia@telar.com   / Vendedor123   -> VENDEDOR
--   carlos@telar.com  / Vendedor123   -> VENDEDOR
--   Cambia estas claves si la base se usa fuera de pruebas.

USE `telar_mx`;
SET NAMES utf8mb4;
START TRANSACTION;

-- ------------------------------------------------------------
-- usuario
-- ------------------------------------------------------------
INSERT INTO `usuario` (`id_usuario`, `nombre`, `correo`, `contrasena`, `rol`) VALUES
(1, 'Administrador',  'admin@telar.com',  '$2a$10$KG2TpmDzF2QpVUxCz9BbQuzeQCl/jD7HqWbK/mBSjdvaD4gV3qN0.', 'ADMIN'),
(2, 'Lucía Ramírez',  'lucia@telar.com',  '$2a$10$dccCvHdDqN4bh5p/mcVG/.3wPBWZp2liOAkTQbw2anPHxUBnVvfjy', 'VENDEDOR'),
(3, 'Carlos Torres',  'carlos@telar.com', '$2a$10$dccCvHdDqN4bh5p/mcVG/.3wPBWZp2liOAkTQbw2anPHxUBnVvfjy', 'VENDEDOR');

-- ------------------------------------------------------------
-- cliente (Persona y Empresa; apellido y razon_social pueden ser NULL)
-- ------------------------------------------------------------
INSERT INTO `cliente`
(`id_cliente`, `tipo_cliente`, `nombre`, `razon_social`, `apellido`, `telefono`, `correo`, `persona_contacto`, `direccion`, `Tipo_Documento`, `Numero_Documento`) VALUES
(1, 'Persona', 'Rosa',    NULL, 'Mendoza Vega',  '987654321', 'rosa.mendoza@correo.com',  NULL, 'Av. Los Telares 245',     'DNI',       '45871236'),
(2, 'Persona', 'Miguel',  NULL, 'Paredes Salas', '976543210', 'miguel.paredes@correo.com', NULL, 'Jr. Las Hilanderas 118', 'DNI',       '71234589'),
(3, 'Persona', 'Sofía',   NULL, 'Castillo Rojas','965432109', NULL,                        NULL, 'Calle Los Algodones 76',  'CE',        '001458796'),
(4, 'Empresa', 'Uniformes Andinos', 'Uniformes Andinos S.A.C.', NULL, '014567890', 'ventas@uniformesandinos.com', 'Jorge Salazar', 'Av. Industrial 1020', 'RUC', '20601234561'),
(5, 'Empresa', 'Boutique Luna',     'Boutique Luna E.I.R.L.',   NULL, '954321098', 'compras@boutiqueluna.com',   'Elena Díaz',    'Galería Central 34',  'RUC', '20487654329');

-- ------------------------------------------------------------
-- proveedor
-- ------------------------------------------------------------
INSERT INTO `proveedor` (`id_proveedor`, `razon_social`, `ruc_rfc`, `telefono`, `correo`, `direccion`) VALUES
(1, 'Textiles del Norte S.A.C.',    '20501112223', '013334455', 'pedidos@textilesnorte.com',  'Av. Las Fábricas 500'),
(2, 'Hilos y Telas Peruanas E.I.R.L.', '20602223334', '012223344', 'ventas@hilosytelas.com',   'Jr. Los Tintes 210'),
(3, 'Alpaca Premium S.A.',          '20403334445', '054221100', 'contacto@alpacapremium.com', 'Parque Industrial Mz. B Lt. 8');

-- ------------------------------------------------------------
-- metodo_pago
-- ------------------------------------------------------------
INSERT INTO `metodo_pago` (`id_metodo_pago`, `nombre`) VALUES
(1, 'Efectivo'),
(2, 'Yape'),
(3, 'Plin'),
(4, 'Tarjeta'),
(5, 'Transferencia');

-- ------------------------------------------------------------
-- producto (incluye precio mayorista y cantidad minima mayorista)
-- ------------------------------------------------------------
INSERT INTO `producto`
(`id_producto`, `nombre`, `descripcion`, `precio`, `precio_mayorista`, `cantidad_minima_mayorista`, `categoria`) VALUES
(1, 'Polo Básico Algodón',      'Polo cuello redondo de algodón pima, corte regular.',  35.00,  28.00, 12, 'Polos'),
(2, 'Camisa Lino Manga Larga',  'Camisa de lino fresca, ideal para clima cálido.',       89.90,  75.00,  6, 'Camisas'),
(3, 'Pantalón Jogger',          'Jogger de algodón perchado con puños elásticos.',       79.90,  65.00, 12, 'Pantalones'),
(4, 'Casaca Ligera',            'Casaca cortaviento con cierre y bolsillos.',           129.90, 110.00,  6, 'Casacas'),
(5, 'Vestido Floral',           'Vestido midi de viscosa con estampado floral.',         99.90,  85.00,  6, 'Vestidos'),
(6, 'Chalina de Alpaca',        'Chalina tejida en alpaca suave, unisex.',               59.90,  48.00, 12, 'Accesorios');

-- ------------------------------------------------------------
-- detalle_producto (variantes: 1-6 primera variante de cada producto)
-- ------------------------------------------------------------
INSERT INTO `detalle_producto` (`id_detalle_producto`, `id_producto`, `color`, `talla`, `material`) VALUES
(1,  1, 'Blanco',       'M',     'Algodón pima'),
(2,  2, 'Beige',        'M',     'Lino'),
(3,  3, 'Negro',        'L',     'Algodón perchado'),
(4,  4, 'Azul marino',  'M',     'Poliéster'),
(5,  5, 'Rojo',         'S',     'Viscosa'),
(6,  6, 'Gris',         'Única', 'Alpaca'),
(7,  1, 'Negro',        'L',     'Algodón pima'),
(8,  1, 'Blanco',       'L',     'Algodón pima'),
(9,  2, 'Blanco',       'L',     'Lino'),
(10, 3, 'Gris',         'M',     'Algodón perchado'),
(11, 4, 'Negro',        'L',     'Poliéster'),
(12, 5, 'Azul',         'S',     'Viscosa');

-- ------------------------------------------------------------
-- stock (los detalles 4 y 9 quedan en alerta: disponible <= minimo)
-- ------------------------------------------------------------
INSERT INTO `stock` (`id_stock`, `id_detalle_producto`, `cantidad_disponible`, `cantidad_minima`) VALUES
(1,  1, 60, 12),
(2,  2, 25,  5),
(3,  3, 18,  6),
(4,  4,  3,  5),
(5,  5, 14,  4),
(6,  6, 30,  6),
(7,  7, 40, 10),
(8,  8, 22, 10),
(9,  9,  0,  5),
(10, 10, 9,  5),
(11, 11, 12, 4),
(12, 12, 7,  4);

-- ------------------------------------------------------------
-- pedido
-- ------------------------------------------------------------
INSERT INTO `pedido` (`id_pedido`, `id_cliente`, `id_usuario`, `fecha_pedido`, `descripcion`, `estado`, `fecha_entrega`) VALUES
(1, 4, 2, '2026-09-15 10:30:00', 'Pedido mayorista de polos y joggers para uniformes', 'Entregado',  '2026-09-22'),
(2, 2, 3, '2026-09-20 16:10:00', 'Camisa de lino para evento familiar',                 'En proceso', '2026-10-02'),
(3, 5, 2, '2026-09-25 11:00:00', 'Chalinas de alpaca para la boutique',                 'Recibido',   '2026-10-10');

INSERT INTO `detalle_pedido` (`id_detalle`, `id_pedido`, `id_detalle_producto`, `cantidad`, `subtotal`) VALUES
(1, 1, 1, 24, 672.00),   -- 24 x 28.00
(2, 1, 3, 12, 780.00),   -- 12 x 65.00
(3, 2, 2,  6, 450.00),   --  6 x 75.00
(4, 3, 6, 12, 576.00);   -- 12 x 48.00

-- ------------------------------------------------------------
-- venta (la venta 1 viene del pedido 1)
-- ------------------------------------------------------------
INSERT INTO `venta` (`id_venta`, `id_pedido`, `id_cliente`, `id_usuario`, `fecha_venta`, `total`) VALUES
(1, 1,    4, 2, '2026-09-22 09:45:00', 1452.00),
(2, NULL, 1, 3, '2026-09-24 12:20:00',  149.80),
(3, NULL, 3, 2, '2026-09-26 17:05:00',   99.90);

INSERT INTO `detalle_venta` (`id_detalle_venta`, `id_venta`, `id_detalle_producto`, `cantidad`, `precio_unitario`) VALUES
(1, 1, 1, 24, 28.00),
(2, 1, 3, 12, 65.00),
(3, 2, 2,  1, 89.90),
(4, 2, 6,  1, 59.90),
(5, 3, 5,  1, 99.90);

INSERT INTO `pago_venta` (`id_pago`, `id_venta`, `id_metodo_pago`, `monto`, `referencia`, `fecha_pago`) VALUES
(1, 1, 5, 1452.00, 'TRF-000123',  '2026-09-22 09:50:00'),
(2, 2, 1,  149.80, NULL,          '2026-09-24 12:22:00'),
(3, 3, 2,   99.90, 'YAPE-778812', '2026-09-26 17:07:00');

INSERT INTO `comprobante` (`id_comprobante`, `id_venta`, `tipo_comprobante`, `serie`, `numero`, `fecha_emision`, `estado`) VALUES
(1, 1, 'Factura', 'F001', 1, '2026-09-22 09:55:00', 'Emitido'),
(2, 2, 'Boleta',  'B001', 1, '2026-09-24 12:25:00', 'Emitido'),
(3, 3, 'Boleta',  'B001', 2, '2026-09-26 17:10:00', 'Emitido');

-- ------------------------------------------------------------
-- compra (reposicion de mercaderia)
-- ------------------------------------------------------------
INSERT INTO `compra` (`id_compra`, `id_proveedor`, `id_usuario`, `fecha_compra`, `numero_comprobante`, `total`) VALUES
(1, 1, 1, '2026-09-01 10:00:00', 'F005-000451', 2250.00),
(2, 2, 1, '2026-09-05 15:30:00', 'F012-000087', 1910.00);

INSERT INTO `detalle_compra` (`id_detalle_compra`, `id_compra`, `id_detalle_producto`, `cantidad`, `precio_compra_unitario`) VALUES
(1, 1, 1, 100, 15.00),   -- 1500.00
(2, 1, 8,  50, 15.00),   --  750.00
(3, 2, 2,  30, 45.00),   -- 1350.00
(4, 2, 6,  20, 28.00);   --  560.00

-- ------------------------------------------------------------
-- cierre_caja
-- ------------------------------------------------------------
INSERT INTO `cierre_caja`
(`id_cierre`, `id_usuario`, `fecha_apertura`, `fecha_cierre`, `monto_inicial`, `monto_final_efectivo`, `total_ventas_calculado`, `estado`) VALUES
(1, 3, '2026-09-24 08:00:00', '2026-09-24 18:00:00', 100.00, 249.80, 149.80, 'Cerrada'),
(2, 2, '2026-09-28 08:00:00', NULL,                  150.00, NULL,     0.00, 'Abierta');

-- ------------------------------------------------------------
-- reporte
-- ------------------------------------------------------------
INSERT INTO `reporte`
(`id_reporte`, `id_usuario`, `tipo`, `fecha_inicio`, `fecha_fin`, `total_vendido`, `id_producto_mas_vendido`, `id_producto_faltante`, `fecha_generacion`) VALUES
(1, 1, 'Ventas mensual', '2026-09-01', '2026-09-30', 1701.70, 1, 4, '2026-09-28 09:00:00');

COMMIT;
