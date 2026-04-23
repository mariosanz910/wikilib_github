package com.tfg.wikilib.repository;

import com.tfg.wikilib.model.Comentario;
import com.tfg.wikilib.model.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByPublicacionOrderByFechaPublicacionDesc(Publicacion publicacion);
}
