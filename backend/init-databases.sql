-- ====================================================================
-- Script de Inicialización de Bases de Datos Centralizadas PostgreSQL
-- Proyecto: OYL Frutos Secos
-- ====================================================================

-- 1. Base de datos para Microservicio de Usuarios y Autenticación
CREATE DATABASE oyl_usuarios
    WITH OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'C'
    LC_CTYPE = 'C'
    TEMPLATE = template0;

-- 2. Base de datos para Microservicio de Productos, Categorías y Stock
CREATE DATABASE oyl_productos
    WITH OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'C'
    LC_CTYPE = 'C'
    TEMPLATE = template0;

-- 3. Base de datos para Microservicio de Ventas y Pedidos
CREATE DATABASE oyl_ventas
    WITH OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'C'
    LC_CTYPE = 'C'
    TEMPLATE = template0;

COMMENT ON DATABASE oyl_usuarios IS 'Almacén de datos exclusivo para ms-usuarios';
COMMENT ON DATABASE oyl_productos IS 'Almacén de datos exclusivo para ms-productos';
COMMENT ON DATABASE oyl_ventas IS 'Almacén de datos exclusivo para ms-ventas';
