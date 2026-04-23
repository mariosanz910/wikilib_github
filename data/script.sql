DROP DATABASE IF EXISTS wikilib;
CREATE DATABASE wikilib;
USE wikilib;

-- Tabla de Usuarios
CREATE TABLE usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100),
    rol ENUM('USUARIO', 'REDACTOR', 'ADMIN') NOT NULL DEFAULT 'USUARIO',
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Categorías (predefinidas, no gestionadas por admin)
CREATE TABLE categoria (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
);

-- Tabla de Entradas (entidad principal, sin colecciones)
CREATE TABLE entrada (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(200) NOT NULL,
    contenido LONGTEXT NOT NULL,
    usuario_id BIGINT NOT NULL,
    categoria_id BIGINT,
    estado ENUM('BORRADOR', 'PUBLICADO') NOT NULL DEFAULT 'BORRADOR',
    valoracion INT NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_publicacion TIMESTAMP NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (categoria_id) REFERENCES categoria(id) ON DELETE SET NULL
);

-- Tabla de Valoraciones (un voto por usuario por entrada)
CREATE TABLE valoracion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entrada_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    tipo ENUM('LIKE', 'DISLIKE') NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (entrada_id, usuario_id),
    FOREIGN KEY (entrada_id) REFERENCES entrada(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabla de Comentarios (comentarios en una entrada)
CREATE TABLE comentario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contenido VARCHAR(100) NOT NULL,
    usuario_id BIGINT NOT NULL,
    entrada_id BIGINT NOT NULL,
    valoracion INT NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (entrada_id) REFERENCES entrada(id) ON DELETE CASCADE
);

-- Tabla de Favoritos
CREATE TABLE favorito (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entrada_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (entrada_id, usuario_id),
    FOREIGN KEY (entrada_id) REFERENCES entrada(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabla de Reportes
CREATE TABLE reporte (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entrada_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    resuelto BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (entrada_id) REFERENCES entrada(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabla de Valoraciones de Comentarios (un voto por usuario por comentario)
CREATE TABLE valoracion_comentario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    comentario_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    tipo ENUM('LIKE', 'DISLIKE') NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (comentario_id, usuario_id),
    FOREIGN KEY (comentario_id) REFERENCES comentario(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Categorías predefinidas ordenadas alfabéticamente
INSERT INTO categoria (nombre, descripcion) VALUES
                                                ('Agricultura', 'Prácticas agrícolas y producción alimentaria'),
                                                ('Antropología', 'Estudio de las culturas y sociedades humanas'),
                                                ('Arquitectura', 'Historia y teoría de la arquitectura'),
                                                ('Arte', 'Historia del arte, estilos y artistas relevantes'),
                                                ('Astronomía', 'El universo, planetas, estrellas y exploración espacial'),
                                                ('Biología', 'Estudio de los seres vivos y sus procesos'),
                                                ('Ciencia', 'Contenido científico y académico'),
                                                ('Cine', 'Análisis cinematográfico, historia del cine y directores'),
                                                ('Derecho', 'Conceptos legales, sistemas jurídicos y derecho comparado'),
                                                ('Economía', 'Conceptos económicos, sistemas y análisis financiero'),
                                                ('Educación', 'Material educativo general'),
                                                ('Estadística', 'Análisis de datos y métodos estadísticos'),
                                                ('Filosofía', 'Corrientes filosóficas, pensadores y reflexiones'),
                                                ('Física', 'Conceptos, teorías y descubrimientos de la física'),
                                                ('Geografía', 'Estudio de territorios, mapas y regiones del mundo'),
                                                ('Historia', 'Contenido histórico y análisis de acontecimientos'),
                                                ('Idiomas', 'Aprendizaje de lenguas y lingüística'),
                                                ('Ingeniería', 'Conceptos y proyectos relacionados con la ingeniería'),
                                                ('Informática', 'Conceptos de computación, sistemas y software'),
                                                ('Inteligencia Artificial', 'Conceptos, aplicaciones y avances en IA'),
                                                ('Lingüística', 'Estudio científico del lenguaje'),
                                                ('Literatura', 'Análisis de obras literarias, autores y corrientes'),
                                                ('Matemáticas', 'Tutoriales y explicaciones matemáticas'),
                                                ('Medicina', 'Información médica, enfermedades y avances sanitarios'),
                                                ('Medio Ambiente', 'Ecología, sostenibilidad y naturaleza'),
                                                ('Música', 'Historia de la música, géneros y análisis musical'),
                                                ('Política', 'Sistemas políticos, historia política y análisis institucional'),
                                                ('Programación', 'Tutoriales y guías de programación'),
                                                ('Psicología', 'Estudio del comportamiento humano y la mente'),
                                                ('Química', 'Explicaciones y contenido educativo sobre química'),
                                                ('Tecnología', 'Noticias y guías tecnológicas'),
                                                ('Viajes', 'Información cultural y geográfica sobre destinos');

-- Usuario administrador (password: admin123)
INSERT INTO usuario (nombre_usuario, email, password, nombre_completo, rol, estado) VALUES
('admin', 'admin@wikilib.com', '$2a$10$8nqiJpYilThnJEXSjrMw0eiJdTxKPKDCp5amXmxjjaaVeevKRlXmK', 'Administrador', 'ADMIN', 'ACTIVO');

-- Usuario redactor de ejemplo (password: admin123)
INSERT INTO usuario (nombre_usuario, email, password, nombre_completo, rol, estado) VALUES
('redactor', 'redactor@wikilib.com', '$2a$10$8nqiJpYilThnJEXSjrMw0eiJdTxKPKDCp5amXmxjjaaVeevKRlXmK', 'Redactor Ejemplo', 'REDACTOR', 'ACTIVO');

-- Usuario lector de ejemplo (password: admin123)
INSERT INTO usuario (nombre_usuario, email, password, nombre_completo, rol, estado) VALUES
('mario', 'mario@wikilib.com', '$2a$10$8nqiJpYilThnJEXSjrMw0eiJdTxKPKDCp5amXmxjjaaVeevKRlXmK', 'Usuario Ejemplo', 'USUARIO', 'ACTIVO');
