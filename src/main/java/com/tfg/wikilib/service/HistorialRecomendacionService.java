package com.tfg.wikilib.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tfg.wikilib.model.HistorialRecomendacion;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.repository.HistorialRecomendacionRepository;

@Service
public class HistorialRecomendacionService {

    private final HistorialRecomendacionRepository historialRepository;

    public HistorialRecomendacionService(HistorialRecomendacionRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    @Transactional
    public void guardarBusqueda(Usuario usuario, String preferencia, String respuesta) {
        HistorialRecomendacion historial = HistorialRecomendacion.builder()
                .usuario(usuario)
                .preferencia(preferencia)
                .respuesta(respuesta)
                .build();
        
        historialRepository.save(historial);
    }

    public List<HistorialRecomendacion> obtenerUltimas3(Usuario usuario) {
        List<HistorialRecomendacion> todas = historialRepository.findByUsuarioOrderByFechaBusquedaDesc(usuario);
        
        // Retornar máximo 3 elementos
        return todas.stream().limit(3).toList();
    }
}