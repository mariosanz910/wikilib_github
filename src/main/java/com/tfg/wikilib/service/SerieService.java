package com.tfg.wikilib.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tfg.wikilib.model.Serie;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.repository.SerieRepository;

import java.util.List;

@Service
public class SerieService {

    @Autowired
    private SerieRepository serieRepository;

    public Page<Serie> obtenerCatalogoSeries(String busqueda, Pageable pageable) {
        if (busqueda != null && !busqueda.isEmpty()) {
            return serieRepository.findByNombreContainingIgnoreCaseOrderByFechaCreacionDesc(busqueda, pageable);
        }
        return serieRepository.findAllByOrderByFechaCreacionDesc(pageable);
    }

    public List<Serie> obtenerSeriesPorAutor(Usuario autor) {
        return serieRepository.findByAutorOrderByFechaCreacionDesc(autor);
    }

    public Serie buscarPorId(Long id) {
        return serieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serie no encontrada con id: " + id));
    }

    @Transactional
    public Serie guardar(Serie serie) {
        return serieRepository.save(serie);
    }

    @Transactional
    public void eliminar(Long id) {
        serieRepository.deleteById(id);
    }
}
