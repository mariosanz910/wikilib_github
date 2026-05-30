package com.tfg.wikilib.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tfg.wikilib.model.Categoria;
import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.Serie;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.service.CategoriaService;
import com.tfg.wikilib.service.PublicacionService;
import com.tfg.wikilib.service.SerieService;
import com.tfg.wikilib.service.UsuarioService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Controller
@RequestMapping("/redactor")
@Validated
public class RedactorController {

    private final PublicacionService publicacionService;
    private final UsuarioService usuarioService;
    private final CategoriaService categoriaService;
    private final SerieService serieService;

    public RedactorController(PublicacionService publicacionService,
                              UsuarioService usuarioService,
                              CategoriaService categoriaService,
                              SerieService serieService) {
        this.publicacionService = publicacionService;
        this.usuarioService = usuarioService;
        this.categoriaService = categoriaService;
        this.serieService = serieService;
    }

    // ===================== PANEL PRINCIPAL =====================

    @GetMapping("/panel")
    public String panel(@RequestParam(required = false) String buscar, Authentication authentication, Model model) {
        Usuario autor = usuarioService.buscarPorNombreUsuario(authentication.getName());
        List<Publicacion> publicaciones = publicacionService.buscarPublicacionesDeAutor(autor, buscar);
        model.addAttribute("publicaciones", publicaciones);
        model.addAttribute("buscar", buscar);
        return "redactor/panel";
    }

    // ===================== SERIES / COLECCIONES =====================

    @GetMapping("/mis-series")
    public String misSeries(Authentication authentication, Model model) {
        Usuario autor = usuarioService.buscarPorNombreUsuario(authentication.getName());
        model.addAttribute("series", serieService.obtenerSeriesPorAutor(autor));
        return "redactor/mis-series";
    }

    @GetMapping("/crear-serie")
    public String crearSerieForm() {
        return "redactor/crear-serie";
    }

    @PostMapping("/crear-serie")
    public String guardarSerie(@RequestParam @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
                               @RequestParam(required = false) @Size(max = 500) String descripcion,
                               Authentication authentication) {
        Usuario autor = usuarioService.buscarPorNombreUsuario(authentication.getName());
        Serie serie = new Serie();
        serie.setNombre(nombre);
        serie.setDescripcion(descripcion);
        serie.setAutor(autor);
        serie.setFechaCreacion(LocalDateTime.now());
        serieService.guardar(serie);
        return "redirect:/redactor/mis-series";
    }

    @GetMapping("/editar-serie/{id}")
    public String editarSerieForm(@PathVariable Long id, Authentication authentication, Model model) {
        Serie serie = serieService.buscarPorId(id);
        if (!serie.getAutor().getNombreUsuario().equals(authentication.getName())) {
            return "redirect:/redactor/mis-series";
        }
        model.addAttribute("serie", serie);
        return "redactor/editar-serie";
    }

    @PostMapping("/editar-serie/{id}")
    public String guardarEdicionSerie(@PathVariable Long id,
                                      @RequestParam @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
                                      @RequestParam(required = false) @Size(max = 500) String descripcion,
                                      Authentication authentication) {
        Serie serie = serieService.buscarPorId(id);
        if (!serie.getAutor().getNombreUsuario().equals(authentication.getName())) {
            return "redirect:/redactor/mis-series";
        }
        serie.setNombre(nombre);
        serie.setDescripcion(descripcion);
        serieService.guardar(serie);
        return "redirect:/redactor/mis-series";
    }

    @PostMapping("/eliminar-serie/{id}")
    public String eliminarSerie(@PathVariable Long id, Authentication authentication) {
        Serie serie = serieService.buscarPorId(id);
        if (serie.getAutor().getNombreUsuario().equals(authentication.getName())) {
            serieService.eliminar(id);
        }
        return "redirect:/redactor/mis-series";
    }

    // ===================== NUEVA PUBLICACIÓN =====================

    // Formulario para crear nueva publicación
    @GetMapping("/nueva-publicacion")
    public String nuevaPublicacionForm(Authentication authentication, Model model) {
        Usuario autor = usuarioService.buscarPorNombreUsuario(authentication.getName());
        model.addAttribute("categorias", categoriaService.obtenerTodas());
        model.addAttribute("series", serieService.obtenerSeriesPorAutor(autor));
        return "redactor/nueva-publicacion";
    }

    // Guardar nueva publicación
    @PostMapping("/nueva-publicacion")
    public String guardarPublicacion(@RequestParam @NotBlank(message = "El título es obligatorio") @Size(max = 200) String titulo,
                                     @RequestParam(required = false) String descripcion,
                                     @RequestParam(required = false) @NotBlank(message = "El texto es obligatorio") String texto,
                                     @RequestParam(required = false) Long categoriaId,
                                     @RequestParam(required = false) Long serieId,
                                     @RequestParam(required = false) Integer orden,
                                     Authentication authentication) {

        Usuario autor = usuarioService.buscarPorNombreUsuario(authentication.getName());

        Publicacion publicacion = new Publicacion();
        publicacion.setTitulo(titulo);
        publicacion.setDescripcion(descripcion);
        publicacion.setTexto(texto);
        publicacion.setAutor(autor);
        publicacion.setFechaCreacion(LocalDateTime.now());

        if (categoriaId != null) {
            Categoria cat = categoriaService.buscarPorId(categoriaId);
            if (cat != null) publicacion.setCategoria(cat);
        }
        
        if (serieId != null) {
            Serie s = serieService.buscarPorId(serieId);
            if (s != null) {
                publicacion.setSerie(s);
                publicacion.setOrden(orden != null ? orden : 1);
            }
        }

        publicacionService.guardar(publicacion);
        return "redirect:/redactor/panel";
    }

    // ===================== EDITAR PUBLICACIÓN =====================

    // Formulario de edición
    @GetMapping("/editar-publicacion/{id}")
    public String editarPublicacionForm(@PathVariable Long id,
                                        Authentication authentication,
                                        Model model) {
        Publicacion publicacion = publicacionService.buscarPorId(id);

        // Verificar que el redactor es el autor
        if (!publicacion.getAutor().getNombreUsuario().equals(authentication.getName())) {
            return "redirect:/redactor/panel";
        }

        Usuario autor = usuarioService.buscarPorNombreUsuario(authentication.getName());
        model.addAttribute("publicacion", publicacion);
        model.addAttribute("categorias", categoriaService.obtenerTodas());
        model.addAttribute("series", serieService.obtenerSeriesPorAutor(autor));
        
        return "redactor/editar-publicacion";
    }

    // Guardar cambios en publicación
    @PostMapping("/editar-publicacion/{id}")
    public String guardarEdicionPublicacion(@PathVariable Long id,
                                            @RequestParam @NotBlank(message = "El título es obligatorio") @Size(max = 200) String titulo,
                                            @RequestParam(required = false) String descripcion,
                                            @RequestParam(required = false) @NotBlank(message = "El texto es obligatorio") String texto,
                                            @RequestParam(required = false) Long categoriaId,
                                            @RequestParam(required = false) Long serieId,
                                            @RequestParam(required = false) Integer orden,
                                            Authentication authentication) {

        Publicacion publicacion = publicacionService.buscarPorId(id);

        if (!publicacion.getAutor().getNombreUsuario().equals(authentication.getName())) {
            return "redirect:/redactor/panel";
        }

        publicacion.setTitulo(titulo);
        publicacion.setDescripcion(descripcion);
        publicacion.setTexto(texto);

        if (categoriaId != null) {
            Categoria cat = categoriaService.buscarPorId(categoriaId);
            if (cat != null) publicacion.setCategoria(cat);
        } else {
            publicacion.setCategoria(null);
        }
        
        if (serieId != null) {
            Serie s = serieService.buscarPorId(serieId);
            if (s != null) {
                publicacion.setSerie(s);
                publicacion.setOrden(orden != null ? orden : 1);
            }
        } else {
            publicacion.setSerie(null);
            publicacion.setOrden(null);
        }

        publicacionService.guardar(publicacion);
        return "redirect:/redactor/panel";
    }

    // ===================== ELIMINAR PUBLICACIÓN =====================

    @PostMapping("/eliminar-publicacion/{id}")
    public String eliminarPublicacion(@PathVariable Long id, Authentication authentication) {
        Publicacion publicacion = publicacionService.buscarPorId(id);

        if (publicacion.getAutor().getNombreUsuario().equals(authentication.getName())) {
            publicacionService.eliminar(id);
        }

        return "redirect:/redactor/panel";
    }
}