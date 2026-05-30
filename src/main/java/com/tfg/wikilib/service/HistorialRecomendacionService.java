package com.tfg.wikilib.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // Top 10 de temas más consultados
    public List<Map<String, Object>> obtenerTop10Temas() {
        List<Object[]> resultados = historialRepository.obtenerTemasMasConsultados();
        
        return resultados.stream()
                .limit(10)
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("tema", (String) row[0]);
                    map.put("cantidad", ((Number) row[1]).longValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Top 10 de publicaciones más recomendadas
    public List<Map<String, Object>> obtenerTop10PublicacionesRecomendadas() {
        List<Object[]> resultados = historialRepository.obtenerPublicacionesMasRecomendadas();
        
        return resultados.stream()
                .limit(10)
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("titulo", (String) row[0]);
                    map.put("cantidad", ((Number) row[1]).longValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Total de búsquedas en el sistema
    public long obtenerTotalBusquedas() {
        return historialRepository.count();
    }

    // Usuarios redactores más activos (por búsquedas)
    public List<Map<String, Object>> obtenerRedactoresMasActivosPorBusquedas() {
        List<Object[]> resultados = historialRepository.obtenerRedactoresMasActivos();
        
        return resultados.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nombreUsuario", (String) row[0]);
                    map.put("nombreCompleto", (String) row[1]);
                    map.put("busquedas", ((Number) row[2]).longValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Usuarios lectores más activos (por búsquedas)
    public List<Map<String, Object>> obtenerLectoresMasActivosPorBusquedas() {
        List<Object[]> resultados = historialRepository.obtenerLectoresMasActivos();
        
        return resultados.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nombreUsuario", (String) row[0]);
                    map.put("nombreCompleto", (String) row[1]);
                    map.put("busquedas", ((Number) row[2]).longValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Promedio de búsquedas por usuario
    public double obtenerPromedioBusquedasPorUsuario() {
        long totalBusquedas = historialRepository.count();
        long totalUsuarios = historialRepository.contarUsuariosConBusquedas();
        
        if (totalUsuarios == 0) {
            return 0;
        }
        
        return (double) totalBusquedas / totalUsuarios;
    }
}