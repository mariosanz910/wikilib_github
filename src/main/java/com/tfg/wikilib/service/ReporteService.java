package com.tfg.wikilib.service;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.Reporte;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.repository.ReporteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final PublicacionService publicacionService;

    public ReporteService(ReporteRepository reporteRepository, PublicacionService publicacionService) {
        this.reporteRepository = reporteRepository;
        this.publicacionService = publicacionService;
    }

    public List<Reporte> obtenerPendientes() {
        return reporteRepository.findByResueltoFalseOrderByFechaReporteDesc();
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
