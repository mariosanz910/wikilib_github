package com.tfg.wikilib.repository;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.model.TipoValoracion;
import com.tfg.wikilib.model.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    Optional<Valoracion> findByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);

    long countByPublicacionAndTipo(Publicacion publicacion, TipoValoracion tipo);
}