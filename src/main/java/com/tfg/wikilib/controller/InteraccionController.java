package com.tfg.wikilib.controller;

import com.tfg.wikilib.model.*;
import com.tfg.wikilib.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/publicacion/{id}")
public class InteraccionController {

    private final ComentarioService comentarioService;
    private final ValoracionService valoracionService;
    private final FavoritoService favoritoService;
    private final ReporteService reporteService;
    private final PublicacionService publicacionService;
    private final UsuarioService usuarioService;

    public InteraccionController(ComentarioService comentarioService,
                                 ValoracionService valoracionService,
                                 FavoritoService favoritoService,
                                 ReporteService reporteService,
                                 PublicacionService publicacionService,
                                 UsuarioService usuarioService) {
        this.comentarioService = comentarioService;
        this.valoracionService = valoracionService;
        this.favoritoService = favoritoService;
        this.reporteService = reporteService;
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
            comentarioService.guardar(comentario);
        }
        
        return "redirect:/publicacion/" + id;
    }

    @PostMapping("/valorar")
    @Transactional
    public String valorar(@PathVariable Long id, @RequestParam boolean esLike, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        
        Usuario usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());
        Publicacion publicacion = publicacionService.buscarPorId(id);

        valoracionService.toggleValoracion(usuario, publicacion, esLike);
        return "redirect:/publicacion/" + id;
    }

    @PostMapping("/favorito")
    public String favorito(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        
        Usuario usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());
        Publicacion publicacion = publicacionService.buscarPorId(id);
        
        favoritoService.toggleFavorito(usuario, publicacion);
        
        return "redirect:/publicacion/" + id;
    }

    @PostMapping("/reportar")
    public String reportar(@PathVariable Long id, @RequestParam String motivo, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        
        Usuario usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());
        Publicacion publicacion = publicacionService.buscarPorId(id);
        
        if (motivo != null && !motivo.trim().isEmpty()) {
            reporteService.reportar(usuario, publicacion, motivo);
        }
        
        return "redirect:/publicacion/" + id + "?reportado=true";
    }
}
