package com.tfg.wikilib.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.TipoValoracion;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.model.Valoracion;
import com.tfg.wikilib.repository.ValoracionRepository;

@Service
public class ValoracionService {

    private final ValoracionRepository valoracionRepository;
    private final PublicacionService publicacionService;

    public ValoracionService(ValoracionRepository valoracionRepository, PublicacionService publicacionService) {
        this.valoracionRepository = valoracionRepository;
        this.publicacionService = publicacionService;
    }

    public long contarPorPublicacionYTipo(Publicacion publicacion, TipoValoracion tipo) {
        return valoracionRepository.countByPublicacionAndTipo(publicacion, tipo);
    }

    public Optional<Valoracion> obtenerMiValoracion(Usuario usuario, Publicacion publicacion) {
        return valoracionRepository.findByUsuarioAndPublicacion(usuario, publicacion);
    }

    @Transactional
    public void toggleValoracion(Usuario usuario, Publicacion publicacion, boolean esLike) {
        TipoValoracion nuevoTipo = esLike ? TipoValoracion.LIKE : TipoValoracion.DISLIKE;

        valoracionRepository.findByUsuarioAndPublicacion(usuario, publicacion).ifPresentOrElse(
            val -> {
                // Si ya valoró, comprobamos si le da al mismo botón para quitar la valoración
                if (val.getTipo() == nuevoTipo) {
                    valoracionRepository.delete(val);
                    // Restamos la valoración que ya existía
                    publicacion.setValoracion(publicacion.getValoracion() + (esLike ? -1 : 1));
                } else {
                    val.setTipo(nuevoTipo);
                    valoracionRepository.save(val);
                    // Cambiamos de like a dislike o viceversa (+2 o -2)
                    publicacion.setValoracion(publicacion.getValoracion() + (esLike ? 2 : -2));
                }
            },
            () -> { // añad una nueva valoración
                Valoracion nueva = new Valoracion();
                nueva.setTipo(nuevoTipo);
                nueva.setUsuario(usuario);
                nueva.setPublicacion(publicacion);
                valoracionRepository.save(nueva);
                // Sumamos o restamos 1
                publicacion.setValoracion(publicacion.getValoracion() + (esLike ? 1 : -1));
            }
        );

        publicacionService.guardar(publicacion);
    }
}
