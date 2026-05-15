package com.tfg.wikilib.service;

import com.tfg.wikilib.model.Comentario;
import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.repository.ComentarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;

    public ComentarioService(ComentarioRepository comentarioRepository) {
        this.comentarioRepository = comentarioRepository;
    }

    public List<Comentario> obtenerPorPublicacion(Publicacion publicacion) {
        return comentarioRepository.findByPublicacionOrderByFechaPublicacionDesc(publicacion);
    }

    @Transactional
    public Comentario guardar(Comentario comentario) {
        return comentarioRepository.save(comentario);
    }
}
