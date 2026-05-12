package com.tfg.wikilib.controller;

import com.tfg.wikilib.model.Categoria;
import com.tfg.wikilib.model.Reporte;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.repository.CategoriaRepository;
import com.tfg.wikilib.repository.ReporteRepository;
import com.tfg.wikilib.service.PublicacionService;
import com.tfg.wikilib.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioService usuarioService;
    private final ReporteRepository reporteRepository;
    private final CategoriaRepository categoriaRepository;
    private final PublicacionService publicacionService;

    public AdminController(UsuarioService usuarioService,
                           ReporteRepository reporteRepository,
                           CategoriaRepository categoriaRepository,
                           PublicacionService publicacionService) {
        this.usuarioService = usuarioService;
        this.reporteRepository = reporteRepository;
        this.categoriaRepository = categoriaRepository;
        this.publicacionService = publicacionService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return "admin/dashboard";
    }

    // ================== GESTIÓN DE USUARIOS ==================
    @GetMapping("/usuarios")
    public String gestionarUsuarios(@RequestParam(required = false) String rol, Model model) {
        if (rol != null && !rol.isEmpty()) {
            try {
                Usuario.Rol rolEnum = Usuario.Rol.valueOf(rol.toUpperCase());
                model.addAttribute("usuarios", usuarioService.buscarPorRol(rolEnum));
            } catch (IllegalArgumentException e) {
                model.addAttribute("usuarios", usuarioService.obtenerTodos());
            }
        } else {
            model.addAttribute("usuarios", usuarioService.obtenerTodos());
        }
        model.addAttribute("filtroRol", rol);
        return "admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/toggle-estado")
    @Transactional
    public String toggleEstadoUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario.getRol() != Usuario.Rol.ADMIN) { // No banear a otros admins
            if (usuario.getEstado() == Usuario.Estado.ACTIVO) {
                usuario.setEstado(Usuario.Estado.INACTIVO);
            } else {
                usuario.setEstado(Usuario.Estado.ACTIVO);
            }
            usuarioService.actualizarUsuario(usuario);
        }
        return "redirect:/admin/usuarios";
    }

    // ================== GESTIÓN DE REPORTES ==================
    @GetMapping("/reportes")
    public String gestionarReportes(Model model) {
        model.addAttribute("reportes", reporteRepository.findByResueltoFalseOrderByFechaReporteDesc());
        return "admin/reportes";
    }

    @PostMapping("/reportes/{id}/resolver")
    @Transactional
    public String resolverReporte(@PathVariable Long id, @RequestParam String accion) {
        Reporte reporte = reporteRepository.findById(id).orElse(null);
        if (reporte == null) return "redirect:/admin/reportes";

        if ("ELIMINAR_PUBLICACION".equals(accion)) {
            // Al eliminar la publicación, el reporte se borra en cascada (ON DELETE CASCADE).
            // No intentamos modificar el reporte después de borrar su publicación.
            publicacionService.eliminar(reporte.getPublicacion().getId());
        } else {
            // DESCARTAR: marcar el reporte como resuelto sin tocar la publicación
            reporte.setResuelto(true);
            reporteRepository.save(reporte);
        }
        return "redirect:/admin/reportes";
    }

    // ================== GESTIÓN DE CATEGORÍAS ==================
    @GetMapping("/categorias")
    public String gestionarCategorias(Model model) {
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "admin/categorias";
    }

    @PostMapping("/categorias/nueva")
    public String nuevaCategoria(@RequestParam String nombre, @RequestParam(required = false) String descripcion) {
        Categoria cat = new Categoria();
        cat.setNombre(nombre);
        cat.setDescripcion(descripcion);
        categoriaRepository.save(cat);
        return "redirect:/admin/categorias";
    }

    // ================== ESTADÍSTICAS ==================
    @GetMapping("/estadisticas")
    public String estadisticas(Model model) {
        model.addAttribute("topPublicaciones", publicacionService.obtenerTop5Leidas());
        model.addAttribute("topRedactores", usuarioService.obtenerRedactoresMasActivos());
        model.addAttribute("topCategorias", categoriaRepository.findMostPopularCategories());
        return "admin/estadisticas";
    }
}