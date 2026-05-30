package com.tfg.wikilib.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.wikilib.model.HistorialRecomendacion;
import com.tfg.wikilib.model.Usuario;

@Repository
public interface HistorialRecomendacionRepository extends JpaRepository<HistorialRecomendacion, Long> {

    // Obtener últimas búsquedas de un usuario, ordenadas por fecha descendente
    List<HistorialRecomendacion> findByUsuarioOrderByFechaBusquedaDesc(Usuario usuario);
}