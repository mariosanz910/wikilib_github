DROP DATABASE IF EXISTS defaultdb;
CREATE DATABASE defaultdb;
USE defaultdb;

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

-- Tabla de Historial de recomendación
CREATE TABLE historial_recomendacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    preferencia VARCHAR(500) NOT NULL,
    respuesta LONGTEXT NOT NULL,
    fecha_busqueda DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    INDEX idx_usuario_fecha (usuario_id, fecha_busqueda DESC)
);

-- Tabla de Series (Colecciones de entradas)
CREATE TABLE serie (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabla de Entradas (entidad principal)
CREATE TABLE entrada (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(200) NOT NULL,
    contenido LONGTEXT NOT NULL,
    usuario_id BIGINT NOT NULL,
    categoria_id BIGINT,
    serie_id BIGINT,
    orden_en_serie INT,
    estado ENUM('BORRADOR', 'PUBLICADO') NOT NULL DEFAULT 'BORRADOR',
    valoracion INT NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_publicacion TIMESTAMP NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (categoria_id) REFERENCES categoria(id) ON DELETE SET NULL,
    FOREIGN KEY (serie_id) REFERENCES serie(id) ON DELETE SET NULL
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
    contenido VARCHAR(1000) NOT NULL,
    usuario_id BIGINT NOT NULL,
    entrada_id BIGINT NOT NULL,
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

-- Inserts de ejemplo para entradas, series, valoraciones, comentarios, favoritos y reportes se pueden agregar posteriormente según sea necesario para pruebas específicas.

-- SERIES
INSERT INTO serie (nombre, descripcion, usuario_id) VALUES
('Historia de Roma', 'Serie sobre la evolución histórica del Imperio Romano', 2),
('Fundamentos de Programación', 'Serie introductoria sobre conceptos básicos de programación', 2),
('Explorando el Universo', 'Serie educativa sobre astronomía y exploración espacial', 2),
('Grandes Filósofos', 'Serie dedicada a pensadores influyentes de la historia', 2),
('Inteligencia Artificial Moderna', 'Serie sobre IA, machine learning y aplicaciones actuales', 2);

-- ENTRADAS
INSERT INTO entrada 
(titulo, contenido, usuario_id, categoria_id, serie_id, orden_en_serie, estado, valoracion, fecha_publicacion)
VALUES

-- HISTORIA DE ROMA
(
'La fundación de Roma',
'Roma, según la tradición, fue fundada en el año 753 a.C. por Rómulo y Remo. La ciudad comenzó como un pequeño asentamiento junto al río Tíber y terminó convirtiéndose en uno de los mayores imperios de la historia.',
2, 16, 1, 1, 'PUBLICADO', 12, NOW()
),

(
'La República Romana',
'La República Romana introdujo un sistema político basado en senadores y magistrados. Este modelo permitió la expansión militar y territorial de Roma durante siglos.',
2, 16, 1, 2, 'PUBLICADO', 15, NOW()
),

(
'Julio César y el fin de la República',
'Julio César fue un líder militar y político cuya influencia marcó el final de la República Romana y el inicio del poder imperial.',
2, 16, 1, 3, 'PUBLICADO', 18, NOW()
),

(
'El Imperio Romano',
'El Imperio Romano alcanzó su máxima expansión bajo el mandato de Trajano y se convirtió en una referencia cultural, militar y económica.',
2, 16, 1, 4, 'PUBLICADO', 21, NOW()
),

(
'La caída de Roma',
'La caída del Imperio Romano de Occidente ocurrió en el año 476 d.C. debido a crisis internas, invasiones bárbaras y problemas económicos.',
2, 16, 1, 5, 'PUBLICADO', 14, NOW()
),

-- PROGRAMACIÓN
(
'¿Qué es programar?',
'Programar consiste en crear instrucciones que un ordenador puede interpretar y ejecutar para resolver problemas específicos.',
2, 28, 2, 1, 'PUBLICADO', 25, NOW()
),

(
'Variables y tipos de datos',
'Las variables permiten almacenar información dentro de un programa. Los tipos de datos definen qué clase de valor puede contener cada variable.',
2, 28, 2, 2, 'PUBLICADO', 19, NOW()
),

(
'Estructuras condicionales',
'Las estructuras condicionales permiten tomar decisiones dentro del flujo de ejecución de un programa.',
2, 28, 2, 3, 'PUBLICADO', 17, NOW()
),

(
'Bucles y repeticiones',
'Los bucles permiten repetir instrucciones múltiples veces hasta cumplir una condición determinada.',
2, 28, 2, 4, 'PUBLICADO', 16, NOW()
),

(
'Funciones en programación',
'Las funciones ayudan a dividir un programa en bloques reutilizables y organizados.',
2, 28, 2, 5, 'PUBLICADO', 22, NOW()
),

(
'Introducción a Java',
'Java es un lenguaje orientado a objetos ampliamente utilizado en aplicaciones empresariales y desarrollo Android.',
2, 28, NULL, NULL, 'PUBLICADO', 30, NOW()
),

(
'Introducción a Python',
'Python destaca por su sintaxis sencilla y su popularidad en ciencia de datos e inteligencia artificial.',
2, 28, NULL, NULL, 'PUBLICADO', 27, NOW()
),

(
'¿Qué es una API?',
'Una API permite la comunicación entre diferentes sistemas y aplicaciones mediante endpoints definidos.',
2, 19, NULL, NULL, 'PUBLICADO', 11, NOW()
),

(
'Bases de datos relacionales',
'Las bases de datos relacionales organizan información mediante tablas relacionadas entre sí.',
2, 19, NULL, NULL, 'PUBLICADO', 20, NOW()
),

(
'Control de versiones con Git',
'Git es un sistema de control de versiones distribuido utilizado para gestionar cambios en proyectos de software.',
2, 31, NULL, NULL, 'PUBLICADO', 23, NOW()
),

-- ASTRONOMÍA
(
'El nacimiento de las estrellas',
'Las estrellas se forman a partir de enormes nubes de gas y polvo conocidas como nebulosas.',
2, 5, 3, 1, 'PUBLICADO', 13, NOW()
),

(
'Los agujeros negros',
'Los agujeros negros poseen una gravedad tan intensa que ni siquiera la luz puede escapar de ellos.',
2, 5, 3, 2, 'PUBLICADO', 29, NOW()
),

(
'El sistema solar',
'El sistema solar está compuesto por el Sol y todos los cuerpos celestes que orbitan a su alrededor.',
2, 5, 3, 3, 'PUBLICADO', 18, NOW()
),

(
'La exploración espacial',
'La exploración espacial ha permitido descubrir información fundamental sobre el universo.',
2, 5, 3, 4, 'PUBLICADO', 15, NOW()
),

(
'La Vía Láctea',
'La Vía Láctea es la galaxia donde se encuentra nuestro sistema solar.',
2, 5, 3, 5, 'PUBLICADO', 17, NOW()
),

-- FILOSOFÍA
(
'Sócrates y el pensamiento crítico',
'Sócrates promovía el diálogo y la reflexión como herramientas fundamentales del conocimiento.',
2, 13, 4, 1, 'PUBLICADO', 12, NOW()
),

(
'Platón y el mundo de las ideas',
'Platón defendía la existencia de un mundo ideal compuesto por formas perfectas.',
2, 13, 4, 2, 'PUBLICADO', 14, NOW()
),

(
'Aristóteles y la lógica',
'Aristóteles desarrolló principios fundamentales de la lógica y el razonamiento.',
2, 13, 4, 3, 'PUBLICADO', 16, NOW()
),

(
'Nietzsche y el superhombre',
'Nietzsche cuestionó la moral tradicional y desarrolló el concepto del superhombre.',
2, 13, 4, 4, 'PUBLICADO', 20, NOW()
),

(
'Descartes y la duda metódica',
'Descartes planteó la famosa frase "Pienso, luego existo" como base del conocimiento.',
2, 13, 4, 5, 'PUBLICADO', 19, NOW()
),

-- IA
(
'¿Qué es la inteligencia artificial?',
'La inteligencia artificial busca desarrollar sistemas capaces de realizar tareas que normalmente requieren inteligencia humana.',
2, 20, 5, 1, 'PUBLICADO', 31, NOW()
),

(
'Machine Learning',
'El aprendizaje automático permite que los sistemas aprendan patrones a partir de datos.',
2, 20, 5, 2, 'PUBLICADO', 28, NOW()
),

(
'Redes neuronales',
'Las redes neuronales artificiales están inspiradas en el funcionamiento del cerebro humano.',
2, 20, 5, 3, 'PUBLICADO', 26, NOW()
),

(
'Deep Learning',
'El deep learning utiliza redes neuronales profundas para resolver problemas complejos.',
2, 20, 5, 4, 'PUBLICADO', 24, NOW()
),

(
'Aplicaciones actuales de la IA',
'La inteligencia artificial se utiliza en medicina, automoción, educación y asistentes virtuales.',
2, 20, 5, 5, 'PUBLICADO', 33, NOW()
),

-- RESTO DE ENTRADAS
(
'La Revolución Francesa',
'La Revolución Francesa transformó profundamente la política y la sociedad europea.',
2, 16, NULL, NULL, 'PUBLICADO', 18, NOW()
),

(
'La Segunda Guerra Mundial',
'La Segunda Guerra Mundial fue uno de los conflictos más devastadores de la historia.',
2, 16, NULL, NULL, 'PUBLICADO', 22, NOW()
),

(
'Introducción a la física cuántica',
'La física cuántica estudia el comportamiento de las partículas a escalas extremadamente pequeñas.',
2, 14, NULL, NULL, 'PUBLICADO', 17, NOW()
),

(
'La teoría de la relatividad',
'Einstein revolucionó la física moderna con la teoría de la relatividad.',
2, 14, NULL, NULL, 'PUBLICADO', 21, NOW()
),

(
'El ADN y la genética',
'El ADN contiene la información genética necesaria para el desarrollo de los seres vivos.',
2, 6, NULL, NULL, 'PUBLICADO', 16, NOW()
),

(
'La evolución de las especies',
'Charles Darwin propuso la teoría de la evolución mediante selección natural.',
2, 6, NULL, NULL, 'PUBLICADO', 19, NOW()
),

(
'Introducción a la economía',
'La economía analiza la producción, distribución y consumo de bienes y servicios.',
2, 10, NULL, NULL, 'PUBLICADO', 12, NOW()
),

(
'Oferta y demanda',
'La ley de oferta y demanda regula gran parte de los mercados económicos.',
2, 10, NULL, NULL, 'PUBLICADO', 11, NOW()
),

(
'La arquitectura gótica',
'La arquitectura gótica se caracteriza por el uso de arcos apuntados y grandes vidrieras.',
2, 3, NULL, NULL, 'PUBLICADO', 10, NOW()
),

(
'El cine de ciencia ficción',
'El cine de ciencia ficción explora futuros posibles y avances tecnológicos imaginarios.',
2, 8, NULL, NULL, 'PUBLICADO', 15, NOW()
),

(
'Introducción a la psicología',
'La psicología estudia el comportamiento humano y los procesos mentales.',
2, 29, NULL, NULL, 'PUBLICADO', 18, NOW()
),

(
'La inteligencia emocional',
'La inteligencia emocional permite comprender y gestionar las emociones propias y ajenas.',
2, 29, NULL, NULL, 'PUBLICADO', 20, NOW()
),

(
'Historia del arte renacentista',
'El Renacimiento marcó una renovación artística y cultural en Europa.',
2, 4, NULL, NULL, 'PUBLICADO', 14, NOW()
),

(
'La música clásica europea',
'La música clásica europea incluye compositores como Mozart, Beethoven y Bach.',
2, 26, NULL, NULL, 'PUBLICADO', 13, NOW()
),

(
'Conceptos básicos de química',
'La química estudia la composición y transformación de la materia.',
2, 30, NULL, NULL, 'PUBLICADO', 12, NOW()
),

(
'La tabla periódica',
'La tabla periódica organiza todos los elementos químicos conocidos.',
2, 30, NULL, NULL, 'PUBLICADO', 16, NOW()
),

(
'Introducción a Linux',
'Linux es un sistema operativo de código abierto ampliamente utilizado en servidores.',
2, 19, NULL, NULL, 'PUBLICADO', 25, NOW()
),

(
'Ciberseguridad básica',
'La ciberseguridad protege sistemas y datos frente a ataques informáticos.',
2, 19, NULL, NULL, 'PUBLICADO', 27, NOW()
),

(
'La historia de Internet',
'Internet revolucionó la comunicación global y el acceso a la información.',
2, 31, NULL, NULL, 'PUBLICADO', 18, NOW()
),

(
'Desarrollo web moderno',
'El desarrollo web moderno utiliza tecnologías frontend y backend avanzadas.',
2, 31, NULL, NULL, 'PUBLICADO', 22, NOW()
),

(
'Introducción al aprendizaje estadístico',
'La estadística permite analizar datos y obtener conclusiones útiles.',
2, 12, NULL, NULL, 'PUBLICADO', 15, NOW()
),

(
'Probabilidad básica',
'La probabilidad estudia la posibilidad de que ocurra un evento.',
2, 12, NULL, NULL, 'PUBLICADO', 13, NOW()
),

(
'Viajar por Japón',
'Japón combina tradición milenaria con tecnología avanzada y modernidad.',
2, 32, NULL, NULL, 'PUBLICADO', 11, NOW()
),

(
'Los ecosistemas marinos',
'Los ecosistemas marinos son fundamentales para la biodiversidad del planeta.',
2, 25, NULL, NULL, 'PUBLICADO', 14, NOW()
),

(
'Introducción al derecho constitucional',
'El derecho constitucional regula la estructura fundamental de un Estado.',
2, 9, NULL, NULL, 'PUBLICADO', 10, NOW()
);

-- SERIE: MY CHEMICAL ROMANCE

INSERT INTO serie (nombre, descripcion, usuario_id)
VALUES
('Historia de My Chemical Romance',
 'Colección dedicada a la historia, evolución, discos y legado de la banda My Chemical Romance.',
 2);

-- ENTRADAS

INSERT INTO entrada
(titulo, contenido, usuario_id, categoria_id, serie_id, orden_en_serie, estado, valoracion, fecha_publicacion)
VALUES

(
'Los inicios de My Chemical Romance',
'My Chemical Romance fue fundada en 2001 en Nueva Jersey por Gerard Way y Matt Pelissier. La banda nació poco después de los atentados del 11 de septiembre, evento que inspiró a Gerard Way a dedicarse a la música.',
2, 26, 6, 1, 'PUBLICADO', 28, NOW()
),

(
'Three Cheers for Sweet Revenge',
'El álbum "Three Cheers for Sweet Revenge" marcó el ascenso internacional de My Chemical Romance gracias a canciones como "Helena" y "I’m Not Okay". El disco consolidó el estilo emo y post-hardcore de la banda.',
2, 26, 6, 2, 'PUBLICADO', 35, NOW()
),

(
'The Black Parade y el éxito mundial',
'"The Black Parade" fue publicado en 2006 y se convirtió en el trabajo más icónico de la banda. El álbum conceptual narra la historia de un personaje conocido como The Patient y contiene canciones legendarias como "Welcome to the Black Parade".',
2, 26, 6, 3, 'PUBLICADO', 42, NOW()
),

(
'La separación de la banda',
'En 2013, My Chemical Romance anunció oficialmente su separación. La noticia impactó profundamente a millones de fans alrededor del mundo y marcó el final de una etapa importante dentro del rock alternativo.',
2, 26, 6, 4, 'PUBLICADO', 31, NOW()
),

(
'El regreso de My Chemical Romance',
'En 2019, la banda anunció su regreso oficial con nuevos conciertos y giras internacionales. El regreso de My Chemical Romance fue recibido con enorme entusiasmo por parte de la comunidad musical y sus seguidores.',
2, 26, 6, 5, 'PUBLICADO', 47, NOW()
);

INSERT INTO reporte (entrada_id, usuario_id, motivo, resuelto, fecha_creacion) VALUES
(
    41,
    3,
    'Contenido potencialmente duplicado respecto a otras publicaciones sobre la banda.',
    FALSE,
    NOW()
),
(
    42,
    3,
    'El artículo contiene información incompleta sobre el contexto del álbum.',
    FALSE,
    NOW()
),
(
    43,
    3,
    'El contenido incluye errores en fechas relacionadas con el lanzamiento del disco.',
    FALSE,
    NOW()
),
(
    44,
    3,
    'Lenguaje demasiado subjetivo y poco neutral para una publicación enciclopédica.',
    FALSE,
    NOW()
),
(
    45,
    3,
    'La publicación necesita más referencias y detalles sobre la gira de regreso.',
    FALSE,
    NOW()
);
