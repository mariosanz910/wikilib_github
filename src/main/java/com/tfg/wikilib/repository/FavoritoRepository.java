package com.tfg.wikilib.repository;

import com.tfg.wikilib.model.Favorito;
import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {
    Optional<Favorito> findByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);
    List<Favorito> findByUsuario(Usuario usuario);
}
