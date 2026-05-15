package com.tfg.wikilib.service;

import com.tfg.wikilib.model.Favorito;
import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.repository.FavoritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;

    public FavoritoService(FavoritoRepository favoritoRepository) {
        this.favoritoRepository = favoritoRepository;
    }

    public boolean esFavorito(Usuario usuario, Publicacion publicacion) {
        return favoritoRepository.findByUsuarioAndPublicacion(usuario, publicacion).isPresent();
    }

    @Transactional
    public void toggleFavorito(Usuario usuario, Publicacion publicacion) {
        favoritoRepository.findByUsuarioAndPublicacion(usuario, publicacion).ifPresentOrElse(
            favoritoRepository::delete, // Si existe, lo borra (toggle)
            () -> {
                Favorito nuevo = new Favorito();
                nuevo.setUsuario(usuario);
                nuevo.setPublicacion(publicacion);
                favoritoRepository.save(nuevo);
            }
        );
    }
}
