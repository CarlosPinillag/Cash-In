-- Tabla de Usuarios
CREATE TABLE `user` (
    id_user BIGINT AUTO_INCREMENT PRIMARY KEY,
    activo BIT(1) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    fecha_registro DATE,
    nombre VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    presupuesto_mensual BIGINT,
    telefono VARCHAR(20)
);

-- Tabla de Tokens
CREATE TABLE auth_token (
    id_token BIGINT AUTO_INCREMENT PRIMARY KEY,
    activo BIT(1),
    expires_at DATETIME(6),
    issued_at DATETIME(6),
    token VARCHAR(512),
    username VARCHAR(255)
);