package com.tfg.wikilib.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.TipoValoracion;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.service.CategoriaService;
import com.tfg.wikilib.service.ComentarioService;
import com.tfg.wikilib.service.FavoritoService;
import com.tfg.wikilib.service.PublicacionService;
import com.tfg.wikilib.service.UsuarioService;
import com.tfg.wikilib.service.ValoracionService;

@Controller
public class HomeController {

    private final PublicacionService publicacionService;
    private final CategoriaService categoriaService;
    private final ComentarioService comentarioService;
    private final ValoracionService valoracionService;
    private final FavoritoService favoritoService;
    private final UsuarioService usuarioService;

    // Constante: 15 resultados por página
    private static final int TAMAÑO_PAGINA = 15;

    public HomeController(PublicacionService publicacionService,
                          CategoriaService categoriaService,
                          ComentarioService comentarioService,
                          ValoracionService valoracionService,
                          FavoritoService favoritoService,
                          UsuarioService usuarioService) {
        this.publicacionService = publicacionService;
        this.categoriaService = categoriaService;
        this.comentarioService = comentarioService;
        this.valoracionService = valoracionService;
        this.favoritoService = favoritoService;
        this.usuarioService = usuarioService;
    }

    // Redirige la raíz al catálogo
    @GetMapping("/")
    public String home() {
        return "redirect:/catalogo";
    }

    // Catálogo con búsqueda por título, filtro por categoría y PAGINACIÓN
    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(required = false) String buscar,
                           @RequestParam(required = false) Long categoria,
                           @RequestParam(required = false) boolean favoritos,
                           @RequestParam(defaultValue = "0") int page,
                           Authentication authentication,
                           Model model) {

        // Validar que page sea >= 0
        if (page < 0) {
            page = 0;
        }

        // Crear objeto Pageable: página actual (0-indexed) y tamaño
        Pageable pageable = PageRequest.of(page, TAMAÑO_PAGINA);

        Page<Publicacion> pagePublicaciones;

        if (favoritos && authentication != null && authentication.isAuthenticated()) {
            // Favoritos: se cargan sin paginar (lista personal)
            Usuario usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());
            List<Publicacion> publicacionesFavoritas = publicacionService.buscarFavoritos(usuario);
            model.addAttribute("favoritosSeleccionado", true);
            
            // Convertir List a Page manualmente (opcional: también puedes paginar aquí)
            int start = page * TAMAÑO_PAGINA;
            int end = Math.min(start + TAMAÑO_PAGINA, publicacionesFavoritas.size());
            List<Publicacion> pageContent = publicacionesFavoritas.subList(start, end);
            pagePublicaciones = new org.springframework.data.domain.PageImpl<>(
                    pageContent,
                    pageable,
                    publicacionesFavoritas.size()
            );
        } else if (buscar != null && !buscar.isBlank()) {
            // Búsqueda por título PAGINADA
            pagePublicaciones = publicacionService.buscarPorTitulo(buscar, pageable);
            model.addAttribute("buscar", buscar);
        } else if (categoria != null) {
            // Filtro por categoría PAGINADO
            pagePublicaciones = publicacionService.buscarPorCategoria(categoria, pageable);
            model.addAttribute("categoriaSeleccionada", categoria);
        } else {
            // Todas las publicaciones PAGINADAS
            pagePublicaciones = publicacionService.obtenerTodas(pageable);
        }

        // Pasar la página de resultados a la vista
        model.addAttribute("page", pagePublicaciones);
        model.addAttribute("publicaciones", pagePublicaciones.getContent());
        model.addAttribute("categorias", categoriaService.obtenerTodas());
        
        // Datos útiles para la vista
        model.addAttribute("totalPages", pagePublicaciones.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("hasPrevious", pagePublicaciones.hasPrevious());
        model.addAttribute("hasNext", pagePublicaciones.hasNext());
        model.addAttribute("totalElements", pagePublicaciones.getTotalElements());

        return "home/catalogo";
    }

    // Ver el detalle de una publicación concreta
    @GetMapping("/publicacion/{id}")
    public String verPublicacion(@PathVariable Long id, Authentication authentication, Model model) {
        publicacionService.incrementarVisitas(id);
        Publicacion publicacion = publicacionService.buscarPorId(id);
        model.addAttribute("publicacion", publicacion);

        // Comentarios ordenados del más reciente al más antiguo
        model.addAttribute("comentarios", comentarioService.obtenerPorPublicacion(publicacion));

        // Conteo de likes y dislikes calculado desde la tabla real (no el campo denormalizado)
        long likes = valoracionService.contarPorPublicacionYTipo(publicacion, TipoValoracion.LIKE);
        long dislikes = valoracionService.contarPorPublicacionYTipo(publicacion, TipoValoracion.DISLIKE);
        model.addAttribute("likesCount", likes);
        model.addAttribute("dislikesCount", dislikes);

        // Comprobar interacción del usuario autenticado (guard contra null/anónimo)
        if (authentication != null && authentication.isAuthenticated()) {
            Usuario usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());

            valoracionService.obtenerMiValoracion(usuario, publicacion).ifPresent(val ->
                model.addAttribute("miValoracion", val.getTipo().name())
            );

            boolean esFavorito = favoritoService.esFavorito(usuario, publicacion);
            model.addAttribute("esFavorito", esFavorito);
        }

        return "home/publicacion";
    }
}