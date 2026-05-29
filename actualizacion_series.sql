CREATE TABLE serie (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    INDEX idx_usuario (usuario_id),
    INDEX idx_fecha (fecha_creacion)
);

ALTER TABLE entrada ADD COLUMN serie_id BIGINT;
ALTER TABLE entrada ADD COLUMN orden_en_serie INT;
ALTER TABLE entrada ADD CONSTRAINT fk_entrada_serie FOREIGN KEY (serie_id) REFERENCES serie(id);
