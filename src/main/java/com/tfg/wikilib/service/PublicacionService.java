package com.tfg.wikilib.service;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.repository.PublicacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final com.tfg.wikilib.repository.FavoritoRepository favoritoRepository;

    public PublicacionService(PublicacionRepository publicacionRepository,
                              com.tfg.wikilib.repository.FavoritoRepository favoritoRepository) {
        this.publicacionRepository = publicacionRepository;
        this.favoritoRepository = favoritoRepository;
    }

    // Obtener todas las publicaciones para el catálogo
    public List<Publicacion> obtenerTodas() {
        return publicacionRepository.findAllByOrderByFechaCreacionDesc();
    }

    // Buscar publicaciones por título
    public List<Publicacion> buscarPorTitulo(String titulo) {
        return publicacionRepository.findByTituloContainingIgnoreCaseOrderByFechaCreacionDesc(titulo);
    }

    // Filtrar publicaciones por categoría
    public List<Publicacion> buscarPorCategoria(Long categoriaId) {
        return publicacionRepository.findByCategoriaIdOrderByFechaCreacionDesc(categoriaId);
    }

    // Obtener publicaciones de un redactor (para su panel)
    public List<Publicacion> obtenerPublicacionesDeAutor(Usuario autor) {
        return publicacionRepository.findByAutorOrderByFechaCreacionDesc(autor);
    }

    // Buscar publicación por ID
    public Publicacion buscarPorId(Long id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada con id: " + id));
    }

    @Transactional
    public void guardar(Publicacion publicacion) {
        publicacionRepository.save(publicacion);
    }

    @Transactional
    public void eliminar(Long id) {
        publicacionRepository.deleteById(id);
    }

    @Transactional
    public void incrementarVisitas(Long id) {
        publicacionRepository.findById(id).ifPresent(p -> {
            p.setVisitas(p.getVisitas() + 1);
            publicacionRepository.save(p);
        });
    }

    public List<Publicacion> obtenerTop5Leidas() {
        return publicacionRepository.findTop5ByOrderByVisitasDesc();
    }

    public List<Publicacion> buscarFavoritos(Usuario usuario) {
        return favoritoRepository.findByUsuario(usuario).stream()
                .map(com.tfg.wikilib.model.Favorito::getPublicacion)
                .toList();
    }
}
