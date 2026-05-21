package com.tfg.wikilib.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.repository.PublicacionRepository;

@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final com.tfg.wikilib.repository.FavoritoRepository favoritoRepository;

    public PublicacionService(PublicacionRepository publicacionRepository,
                              com.tfg.wikilib.repository.FavoritoRepository favoritoRepository) {
        this.publicacionRepository = publicacionRepository;
        this.favoritoRepository = favoritoRepository;
    }

    // Obtener todas las publicaciones PAGINADAS
    public Page<Publicacion> obtenerTodas(Pageable pageable) {
        return publicacionRepository.findAllByOrderByFechaCreacionDesc(pageable);
    }

    // Buscar publicaciones por título PAGINADAS
    public Page<Publicacion> buscarPorTitulo(String titulo, Pageable pageable) {
        return publicacionRepository.findByTituloContainingIgnoreCaseOrderByFechaCreacionDesc(titulo, pageable);
    }

    // Filtrar publicaciones por categoría PAGINADAS
    public Page<Publicacion> buscarPorCategoria(Long categoriaId, Pageable pageable) {
        return publicacionRepository.findByCategoriaIdOrderByFechaCreacionDesc(categoriaId, pageable);
    }

    // Obtener publicaciones de un redactor (para su panel, sin paginar)
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

    // Buscar favoritos (sin paginar, es una lista personal del usuario)
    public List<Publicacion> buscarFavoritos(Usuario usuario) {
        return favoritoRepository.findByUsuario(usuario).stream()
                .map(com.tfg.wikilib.model.Favorito::getPublicacion)
                .toList();
    }
}