package com.tfg.wikilib.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.model.Serie;
import java.util.Optional;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    // Todas las publicaciones PAGINADAS
    Page<Publicacion> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    // Buscar por título PAGINADO
    Page<Publicacion> findByTituloContainingIgnoreCaseOrderByFechaCreacionDesc(String titulo, Pageable pageable);

    // Filtrar por categoría PAGINADO
    Page<Publicacion> findByCategoriaIdOrderByFechaCreacionDesc(Long categoriaId, Pageable pageable);

    // Publicaciones de un autor concreto (sin paginar, para panel del redactor)
    List<Publicacion> findByAutorOrderByFechaCreacionDesc(Usuario autor);

    // Estadísticas (sin paginar)
    List<Publicacion> findTop5ByOrderByVisitasDesc();

    // Navegación en series
    List<Publicacion> findBySerieOrderByOrdenAsc(Serie serie);
    
    Optional<Publicacion> findFirstBySerieAndOrdenGreaterThanOrderByOrdenAsc(Serie serie, Integer orden);
    
    Optional<Publicacion> findFirstBySerieAndOrdenLessThanOrderByOrdenDesc(Serie serie, Integer orden);
}