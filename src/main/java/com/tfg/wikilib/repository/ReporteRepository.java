package com.tfg.wikilib.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tfg.wikilib.model.Reporte;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    // Obtener reportes pendientes sin paginar (para uso interno)
    List<Reporte> findByResueltoFalseOrderByFechaReporteDesc();

    // Obtener reportes pendientes PAGINADOS
    Page<Reporte> findByResueltoFalseOrderByFechaReporteDesc(Pageable pageable);

    // Buscar reportes por nombre de publicación PAGINADO
    @Query("SELECT r FROM Reporte r WHERE r.resuelto = false AND LOWER(r.publicacion.titulo) LIKE LOWER(CONCAT('%', :buscar, '%')) ORDER BY r.fechaReporte DESC")
    Page<Reporte> buscarPorTituloPublicacionPendientes(@Param("buscar") String buscar, Pageable pageable);
}