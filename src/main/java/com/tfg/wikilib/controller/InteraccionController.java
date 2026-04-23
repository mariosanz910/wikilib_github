package com.tfg.wikilib.controller;

import com.tfg.wikilib.model.*;
import com.tfg.wikilib.repository.*;
import com.tfg.wikilib.service.PublicacionService;
import com.tfg.wikilib.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/publicacion/{id}")
public class InteraccionController {

    private final ComentarioRepository comentarioRepository;
    private final ValoracionRepository valoracionRepository;
    private final FavoritoRepository favoritoRepository;
    private final ReporteRepository reporteRepository;
    private final PublicacionService publicacionService;
    private final UsuarioService usuarioService;

    public InteraccionController(ComentarioRepository comentarioRepository,
                                 ValoracionRepository valoracionRepository,
                                 FavoritoRepository favoritoRepository,
                                 ReporteRepository reporteRepository,
                                 PublicacionService publicacionService,
                                 UsuarioService usuarioService) {
        this.comentarioRepository = comentarioRepository;
        this.valoracionRepository = valoracionRepository;
        this.favoritoRepository = favoritoRepository;
        this.reporteRepository = reporteRepository;
        this.publicacionService = publicacionService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/comentar")
    public String comentar(@PathVariable Long id, @RequestParam String contenido, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        
        Usuario usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());
        Publicacion publicacion = publicacionService.buscarPorId(id);
        
        if (contenido != null && !contenido.trim().isEmpty()) {
            Comentario comentario = new Comentario();
            comentario.setContenido(contenido);
            comentario.setAutor(usuario);
            comentario.setPublicacion(publicacion);
            comentario.setFechaPublicacion(LocalDateTime.now());
            comentarioRepository.save(comentario);
        }
        
        return "redirect:/publicacion/" + id;
    }

    @PostMapping("/valorar")
    public String valorar(@PathVariable Long id, @RequestParam boolean esLike, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        
        Usuario usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());
        Publicacion publicacion = publicacionService.buscarPorId(id);

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
            () -> {
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
        return "redirect:/publicacion/" + id;
    }

    @PostMapping("/favorito")
    public String favorito(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        
        Usuario usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());
        Publicacion publicacion = publicacionService.buscarPorId(id);
        
        favoritoRepository.findByUsuarioAndPublicacion(usuario, publicacion).ifPresentOrElse(
            favoritoRepository::delete, // Si existe, lo borra (toggle)
            () -> {
                Favorito nuevo = new Favorito();
                nuevo.setUsuario(usuario);
                nuevo.setPublicacion(publicacion);
                favoritoRepository.save(nuevo);
            }
        );
        
        return "redirect:/publicacion/" + id;
    }

    @PostMapping("/reportar")
    public String reportar(@PathVariable Long id, @RequestParam String motivo, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        
        Usuario usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());
        Publicacion publicacion = publicacionService.buscarPorId(id);
        
        if (motivo != null && !motivo.trim().isEmpty()) {
            Reporte reporte = new Reporte();
            reporte.setMotivo(motivo);
            reporte.setUsuario(usuario);
            reporte.setPublicacion(publicacion);
            reporte.setFechaReporte(LocalDateTime.now());
            reporte.setResuelto(false);
            reporteRepository.save(reporte);
        }
        
        return "redirect:/publicacion/" + id + "?reportado=true";
    }
}
