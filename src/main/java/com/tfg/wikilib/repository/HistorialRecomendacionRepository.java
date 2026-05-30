package com.tfg.wikilib.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tfg.wikilib.model.HistorialRecomendacion;
import com.tfg.wikilib.model.Usuario;

@Repository
public interface HistorialRecomendacionRepository extends JpaRepository<HistorialRecomendacion, Long> {

    // Obtener últimas búsquedas de un usuario, ordenadas por fecha descendente
    List<HistorialRecomendacion> findByUsuarioOrderByFechaBusquedaDesc(Usuario usuario);

     // Top 10 temas más consultados
    @Query(value = "SELECT preferencia, COUNT(*) as cantidad FROM historial_recomendacion GROUP BY preferencia ORDER BY cantidad DESC LIMIT 10", nativeQuery = true)
    List<Object[]> obtenerTemasMasConsultados();

    // Top 10 publicaciones más recomendadas (extrae títulos de la respuesta)
    @Query(value = "SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(respuesta, '- ', -1), CHAR(10), 1) as titulo, COUNT(*) as cantidad FROM historial_recomendacion WHERE respuesta LIKE '%-  %' GROUP BY SUBSTRING_INDEX(SUBSTRING_INDEX(respuesta, '- ', -1), CHAR(10), 1) ORDER BY cantidad DESC LIMIT 10", nativeQuery = true)
    List<Object[]> obtenerPublicacionesMasRecomendadas();

    // Redactores más activos por búsquedas (rol = REDACTOR)
    @Query(value = "SELECT u.nombre_usuario, u.nombre_completo, COUNT(h.id) as busquedas FROM historial_recomendacion h JOIN usuario u ON h.usuario_id = u.id WHERE u.rol = 'REDACTOR' GROUP BY u.id ORDER BY busquedas DESC LIMIT 10", nativeQuery = true)
    List<Object[]> obtenerRedactoresMasActivos();

    // Lectores más activos por búsquedas (rol = USUARIO)
    @Query(value = "SELECT u.nombre_usuario, u.nombre_completo, COUNT(h.id) as busquedas FROM historial_recomendacion h JOIN usuario u ON h.usuario_id = u.id WHERE u.rol = 'USUARIO' GROUP BY u.id ORDER BY busquedas DESC LIMIT 10", nativeQuery = true)
    List<Object[]> obtenerLectoresMasActivos();

    // Contar usuarios únicos con búsquedas
    @Query(value = "SELECT COUNT(DISTINCT usuario_id) FROM historial_recomendacion", nativeQuery = true)
    long contarUsuariosConBusquedas();
}
