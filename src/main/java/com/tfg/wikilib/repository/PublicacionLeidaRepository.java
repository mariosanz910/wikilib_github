package com.tfg.wikilib.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.PublicacionLeida;
import com.tfg.wikilib.model.Usuario;

public interface PublicacionLeidaRepository extends JpaRepository<PublicacionLeida, Long> {

    boolean existsByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);

    // IDs de publicaciones leídas por un usuario (para filtrar en catálogo)
    @Query("SELECT pl.publicacion.id FROM PublicacionLeida pl WHERE pl.usuario = :usuario")
    List<Long> findPublicacionIdsByUsuario(@Param("usuario") Usuario usuario);
}