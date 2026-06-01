package com.tfg.wikilib.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.TipoValoracion;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.service.CategoriaService;
import com.tfg.wikilib.service.ComentarioService;
import com.tfg.wikilib.service.FavoritoService;
import com.tfg.wikilib.service.HistorialRecomendacionService;
import com.tfg.wikilib.service.PublicacionLeidaService;
import com.tfg.wikilib.service.PublicacionService;
import com.tfg.wikilib.service.RecomendacionService;
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
    private final RecomendacionService recomendacionService;
    private final HistorialRecomendacionService historialRecomendacionService;
    private final PublicacionLeidaService publicacionLeidaService;

    // Constante: 15 resultados por página
    private static final int TAMAÑO_PAGINA = 15;

    public HomeController(PublicacionService publicacionService,
                          CategoriaService categoriaService,
                          ComentarioService comentarioService,
                          ValoracionService valoracionService,
                          FavoritoService favoritoService,
                          UsuarioService usuarioService,
                          RecomendacionService recomendacionService,
                          HistorialRecomendacionService historialRecomendacionService,
                          PublicacionLeidaService publicacionLeidaService) {
        this.publicacionService = publicacionService;
        this.categoriaService = categoriaService;
        this.comentarioService = comentarioService;
        this.valoracionService = valoracionService;
        this.favoritoService = favoritoService;
        this.usuarioService = usuarioService;
        this.recomendacionService = recomendacionService;
        this.historialRecomendacionService = historialRecomendacionService;
        this.publicacionLeidaService = publicacionLeidaService;
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
                           // String en lugar de boolean para evitar errores de conversión
                           // con valores "on" (checkbox) y "true" (links de paginación)
                           @RequestParam(required = false) String favoritos,
                           @RequestParam(required = false) String ocultarLeidas,
                           @RequestParam(defaultValue = "0") int page,
                           Authentication authentication,
                           Model model) {

        // Convertir los parámetros String a boolean manualmente.
        // "on"   → checkbox marcado (envío de formulario HTML)
        // "true" → link de paginación generado por Thymeleaf
        // cualquier otro valor (null, "", "false") → false
        boolean favoritesFlag = "true".equals(favoritos) || "on".equals(favoritos);
        boolean ocultarLeidasFlag = "true".equals(ocultarLeidas) || "on".equals(ocultarLeidas);

        // Validar que page sea >= 0
        if (page < 0) {
            page = 0;
        }

        // Crear objeto Pageable: página actual (0-indexed) y tamaño
        Pageable pageable = PageRequest.of(page, TAMAÑO_PAGINA);

        Page<Publicacion> pagePublicaciones;

        // Obtener IDs leídas si procede
        boolean filtrarLeidas = ocultarLeidasFlag && authentication != null && authentication.isAuthenticated();
        List<Long> idsLeidas = List.of();
        if (filtrarLeidas) {
            Usuario usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());
            idsLeidas = publicacionLeidaService.obtenerIdsLeidas(usuario);
            if (idsLeidas.isEmpty()) {
                filtrarLeidas = false; // nada que excluir, evitar query IN vacío
            }
        }

        if (favoritesFlag && authentication != null && authentication.isAuthenticated()) {
            // Favoritos: se cargan sin paginar (lista personal)
            Usuario usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());
            List<Publicacion> publicacionesFavoritas = publicacionService.buscarFavoritos(usuario);

            // Filtrar leídas en memoria si está activo
            if (filtrarLeidas) {
                final List<Long> ids = idsLeidas;
                publicacionesFavoritas = publicacionesFavoritas.stream()
                        .filter(p -> !ids.contains(p.getId()))
                        .toList();
            }

            // Convertir List a Page manualmente
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
            model.addAttribute("buscar", buscar);
            pagePublicaciones = filtrarLeidas
                    ? publicacionService.buscarPorTituloExcluyendo(buscar, idsLeidas, pageable)
                    : publicacionService.buscarPorTitulo(buscar, pageable);
        } else if (categoria != null) {
            // Filtro por categoría PAGINADO
            model.addAttribute("categoriaSeleccionada", categoria);
            pagePublicaciones = filtrarLeidas
                    ? publicacionService.buscarPorCategoriaExcluyendo(categoria, idsLeidas, pageable)
                    : publicacionService.buscarPorCategoria(categoria, pageable);
        } else {
            // Todas las publicaciones PAGINADAS
            pagePublicaciones = filtrarLeidas
                    ? publicacionService.obtenerTodasExcluyendo(idsLeidas, pageable)
                    : publicacionService.obtenerTodas(pageable);
        }

        // Pasar la página de resultados a la vista
        model.addAttribute("page", pagePublicaciones);
        model.addAttribute("publicaciones", pagePublicaciones.getContent());
        model.addAttribute("categorias", categoriaService.obtenerTodas());

        // Pasar los flags booleanos reales a la vista (para los links de paginación y checkboxes)
        model.addAttribute("favoritosSeleccionado", favoritesFlag);
        model.addAttribute("ocultarLeidas", ocultarLeidasFlag);

        // Datos útiles para la vista
        model.addAttribute("totalPages", pagePublicaciones.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("hasPrevious", pagePublicaciones.hasPrevious());
        model.addAttribute("hasNext", pagePublicaciones.hasNext());
        model.addAttribute("totalElements", pagePublicaciones.getTotalElements());

        // Agregar historial de recomendaciones si el usuario está autenticado
        if (authentication != null && authentication.isAuthenticated()) {
            Usuario usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());
            model.addAttribute("historialRecomendaciones", historialRecomendacionService.obtenerUltimas3(usuario));
        }

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

            // Marcar como leída automáticamente
            publicacionLeidaService.marcarComoLeida(usuario, publicacion);

            valoracionService.obtenerMiValoracion(usuario, publicacion).ifPresent(val ->
                model.addAttribute("miValoracion", val.getTipo().name())
            );

            boolean esFavorito = favoritoService.esFavorito(usuario, publicacion);
            model.addAttribute("esFavorito", esFavorito);
        }

        // Navegación de series
        if (publicacion.getSerie() != null) {
            Optional<Publicacion> anterior = publicacionService.obtenerAnteriorEnSerie(publicacion);
            Optional<Publicacion> siguiente = publicacionService.obtenerSiguienteEnSerie(publicacion);

            anterior.ifPresent(p -> model.addAttribute("publicacionAnterior", p));
            siguiente.ifPresent(p -> model.addAttribute("publicacionSiguiente", p));
        }

        return "home/publicacion";
    }

    @PostMapping("/api/recomendacion")
    public ResponseEntity<Map<String, Object>> obtenerRecomendacion(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String preferencia = body.getOrDefault("preferencia", "").trim();

        Usuario usuario = null;
        if (authentication != null && authentication.isAuthenticated()) {
            usuario = usuarioService.buscarPorNombreUsuario(authentication.getName());
        }

        return ResponseEntity.ok(recomendacionService.obtenerRecomendacion(preferencia, usuario));
    }
}