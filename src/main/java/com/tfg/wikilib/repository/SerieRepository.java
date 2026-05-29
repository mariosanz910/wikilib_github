package com.tfg.wikilib.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.wikilib.model.Serie;
import com.tfg.wikilib.model.Usuario;
import java.util.List;

@Repository
public interface SerieRepository extends JpaRepository<Serie, Long> {

    Page<Serie> findAllByOrderByFechaCreacionDesc(Pageable pageable);
    
    Page<Serie> findByNombreContainingIgnoreCaseOrderByFechaCreacionDesc(String nombre, Pageable pageable);
    
    List<Serie> findByAutorOrderByFechaCreacionDesc(Usuario autor);
}
