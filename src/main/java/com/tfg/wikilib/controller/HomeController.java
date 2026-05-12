package com.tfg.wikilib.controller;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.TipoValoracion;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.repository.CategoriaRepository;
import com.tfg.wikilib.repository.ComentarioRepository;
import com.tfg.wikilib.repository.FavoritoRepository;
import com.tfg.wikilib.repository.ValoracionRepository;
import com.tfg.wikilib.service.PublicacionService;
import com.tfg.wikilib.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {

    private final PublicacionService publicacionService;
    private final CategoriaRepository categoriaRepository;
    private final ComentarioRepository comentarioRepository;
    private final ValoracionRepository valoracionRepository;
    private final FavoritoRepository favoritoRepository;
    private final UsuarioService usuarioService;

    public HomeController(PublicacionService publicacionService,
                          CategoriaRepository categoriaRepository,
                          ComentarioRepository comentarioRepository,
                          ValoracionRepository valoracionRepository,
                          FavoritoRepository favoritoRepository,
                          UsuarioService usuarioService) {
        this.publicacionService = publicacionService;
        this.categoriaRepository = categoriaRepository;
        this.comentarioRepository = comentarioRepository;
        this.valoracionRepository = valoracionRepository;
        this.favoritoRepository = favoritoRepository;
        this.usuarioService = usuarioService;
    }

    // Redirige la raíz al catálogo
    @GetMapping("/")
    public String home() {
        return "redirect:/catalogo";
    }

    // Catálogo con búsqueda por título y filtro por categoría
    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(required = false) String buscar,
                           @RequestParam(required = false) Long categoria,
                           @RequestParam(required = false) boolean favoritos,
                           Authentication authentication,
                           Model model) {

        List<Publicacion> publicaciones;

        if (favoritos && authentication != null && authentication.isAuthenticated()) {
            Usuario usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());
            publicaciones = publicacionService.buscarFavoritos(usuario);
            model.addAttribute("favoritosSeleccionado", true);
        } else if (buscar != null && !buscar.isBlank()) {
            publicaciones = publicacionService.buscarPorTitulo(buscar);
            model.addAttribute("buscar", buscar);
        } else if (categoria != null) {
            publicaciones = publicacionService.buscarPorCategoria(categoria);
            model.addAttribute("categoriaSeleccionada", categoria);
        } else {
            publicaciones = publicacionService.obtenerTodas();
        }

        model.addAttribute("publicaciones", publicaciones);
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "home/catalogo";
    }

    // Ver el detalle de una publicación concreta
    @GetMapping("/publicacion/{id}")
    public String verPublicacion(@PathVariable Long id, Authentication authentication, Model model) {
        publicacionService.incrementarVisitas(id);
        Publicacion publicacion = publicacionService.buscarPorId(id);
        model.addAttribute("publicacion", publicacion);

        // Comentarios ordenados del más reciente al más antiguo
        model.addAttribute("comentarios", comentarioRepository.findByPublicacionOrderByFechaPublicacionDesc(publicacion));

        // Conteo de likes y dislikes calculado desde la tabla real (no el campo denormalizado)
        long likes = valoracionRepository.countByPublicacionAndTipo(publicacion, TipoValoracion.LIKE);
        long dislikes = valoracionRepository.countByPublicacionAndTipo(publicacion, TipoValoracion.DISLIKE);
        model.addAttribute("likesCount", likes);
        model.addAttribute("dislikesCount", dislikes);

        // Comprobar interacción del usuario autenticado (guard contra null/anónimo)
        if (authentication != null && authentication.isAuthenticated()) {
            Usuario usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());

            valoracionRepository.findByUsuarioAndPublicacion(usuario, publicacion).ifPresent(val ->
                model.addAttribute("miValoracion", val.getTipo().name())
            );

            boolean esFavorito = favoritoRepository.findByUsuarioAndPublicacion(usuario, publicacion).isPresent();
            model.addAttribute("esFavorito", esFavorito);
        }

        return "home/publicacion";
    }
}