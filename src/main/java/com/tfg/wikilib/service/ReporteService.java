package com.tfg.wikilib.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.Reporte;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.repository.ReporteRepository;

@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final PublicacionService publicacionService;

    public ReporteService(ReporteRepository reporteRepository, PublicacionService publicacionService) {
        this.reporteRepository = reporteRepository;
        this.publicacionService = publicacionService;
    }

    // Obtener reportes pendientes sin paginar (para uso interno)
    public List<Reporte> obtenerPendientes() {
        return reporteRepository.findByResueltoFalseOrderByFechaReporteDesc();
    }

    // Obtener reportes pendientes PAGINADOS
    public Page<Reporte> obtenerPendientesPaginados(Pageable pageable) {
        return reporteRepository.findByResueltoFalseOrderByFechaReporteDesc(pageable);
    }

    // Buscar reportes por nombre de publicación PAGINADO
    public Page<Reporte> buscarPorTituloPublicacion(String buscar, Pageable pageable) {
        return reporteRepository.buscarPorTituloPublicacionPendientes(buscar, pageable);
    }

    @Transactional
    public Reporte reportar(Usuario usuario, Publicacion publicacion, String motivo) {
        Reporte reporte = new Reporte();
        reporte.setMotivo(motivo);
        reporte.setUsuario(usuario);
        reporte.setPublicacion(publicacion);
        reporte.setFechaReporte(LocalDateTime.now());
        reporte.setResuelto(false);
        return reporteRepository.save(reporte);
    }

    @Transactional
    public void resolverReporte(Long id, String accion) {
        Reporte reporte = reporteRepository.findById(id).orElse(null);
        if (reporte == null) return;

        if ("ELIMINAR_PUBLICACION".equals(accion)) {
            Long publicacionId = reporte.getPublicacion().getId();
            reporteRepository.delete(reporte);
            publicacionService.eliminar(publicacionId);
        } else {
            reporte.setResuelto(true);
            reporteRepository.save(reporte);
        }
    }
}