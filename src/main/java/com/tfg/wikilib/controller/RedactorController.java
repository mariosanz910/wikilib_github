package com.tfg.wikilib.controller;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.repository.CategoriaRepository;
import com.tfg.wikilib.service.PublicacionService;
import com.tfg.wikilib.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/redactor")
public class RedactorController {

    private final PublicacionService publicacionService;
    private final UsuarioService usuarioService;
    private final CategoriaRepository categoriaRepository;

    public RedactorController(PublicacionService publicacionService,
                              UsuarioService usuarioService,
                              CategoriaRepository categoriaRepository) {
        this.publicacionService = publicacionService;
        this.usuarioService = usuarioService;
        this.categoriaRepository = categoriaRepository;
    }

    // ===================== PANEL PRINCIPAL =====================

    @GetMapping("/panel")
    public String panel(Authentication authentication, Model model) {
        Usuario autor = usuarioService.buscarPorNombreUsuario(authentication.getName());
        List<Publicacion> publicaciones = publicacionService.obtenerPublicacionesDeAutor(autor);
        model.addAttribute("publicaciones", publicaciones);
        return "redactor/panel";
    }

    // ===================== NUEVA PUBLICACIÓN =====================

    // Formulario para crear nueva publicación
    @GetMapping("/nueva-publicacion")
    public String nuevaPublicacionForm(Model model) {
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "redactor/nueva-publicacion";
    }

    // Guardar nueva publicación
    @PostMapping("/nueva-publicacion")
    public String guardarPublicacion(@RequestParam String titulo,
                                     @RequestParam(required = false) String descripcion,
                                     @RequestParam(required = false) String texto,
                                     @RequestParam(required = false) Long categoriaId,
                                     Authentication authentication) {

        Usuario autor = usuarioService.buscarPorNombreUsuario(authentication.getName());

        Publicacion publicacion = new Publicacion();
        publicacion.setTitulo(titulo);
        publicacion.setDescripcion(descripcion);
        publicacion.setTexto(texto);
        publicacion.setAutor(autor);
        publicacion.setFechaCreacion(LocalDateTime.now());

        if (categoriaId != null) {
            categoriaRepository.findById(categoriaId).ifPresent(publicacion::setCategoria);
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

        model.addAttribute("publicacion", publicacion);
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "redactor/editar-publicacion";
    }

    // Guardar cambios en publicación
    @PostMapping("/editar-publicacion/{id}")
    public String guardarEdicionPublicacion(@PathVariable Long id,
                                            @RequestParam String titulo,
                                            @RequestParam(required = false) String descripcion,
                                            @RequestParam(required = false) String texto,
                                            @RequestParam(required = false) Long categoriaId,
                                            Authentication authentication) {

        Publicacion publicacion = publicacionService.buscarPorId(id);

        if (!publicacion.getAutor().getNombreUsuario().equals(authentication.getName())) {
            return "redirect:/redactor/panel";
        }

        publicacion.setTitulo(titulo);
        publicacion.setDescripcion(descripcion);
        publicacion.setTexto(texto);

        if (categoriaId != null) {
            categoriaRepository.findById(categoriaId).ifPresent(publicacion::setCategoria);
        } else {
            publicacion.setCategoria(null);
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