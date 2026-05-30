package com.tfg.wikilib.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.PublicacionLeida;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.repository.PublicacionLeidaRepository;

@Service
public class PublicacionLeidaService {

    private final PublicacionLeidaRepository publicacionLeidaRepository;

    public PublicacionLeidaService(PublicacionLeidaRepository publicacionLeidaRepository) {
        this.publicacionLeidaRepository = publicacionLeidaRepository;
    }

    /**
     * Marca una publicación como leída por el usuario.
     * Si ya está marcada, no hace nada (idempotente).
     */
    @Transactional
    public void marcarComoLeida(Usuario usuario, Publicacion publicacion) {
        if (!publicacionLeidaRepository.existsByUsuarioAndPublicacion(usuario, publicacion)) {
            PublicacionLeida leida = PublicacionLeida.builder()
                    .usuario(usuario)
                    .publicacion(publicacion)
                    .build();
            publicacionLeidaRepository.save(leida);
        }
    }

    public List<Long> obtenerIdsLeidas(Usuario usuario) {
        return publicacionLeidaRepository.findPublicacionIdsByUsuario(usuario);
    }

    public boolean esLeida(Usuario usuario, Publicacion publicacion) {
        return publicacionLeidaRepository.existsByUsuarioAndPublicacion(usuario, publicacion);
    }
}