package com.tfg.wikilib.controller;

import com.tfg.wikilib.model.Categoria;
import com.tfg.wikilib.model.Reporte;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.service.CategoriaService;
import com.tfg.wikilib.service.ReporteService;
import com.tfg.wikilib.service.PublicacionService;
import com.tfg.wikilib.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Controller
@RequestMapping("/admin")
@Validated
public class AdminController {

    private final UsuarioService usuarioService;
    private final ReporteService reporteService;
    private final CategoriaService categoriaService;
    private final PublicacionService publicacionService;

    public AdminController(UsuarioService usuarioService,
                           ReporteService reporteService,
                           CategoriaService categoriaService,
                           PublicacionService publicacionService) {
        this.usuarioService = usuarioService;
        this.reporteService = reporteService;
        this.categoriaService = categoriaService;
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
        model.addAttribute("reportes", reporteService.obtenerPendientes());
        return "admin/reportes";
    }

    @PostMapping("/reportes/{id}/resolver")
    public String resolverReporte(@PathVariable Long id, @RequestParam String accion) {
        reporteService.resolverReporte(id, accion);
        return "redirect:/admin/reportes";
    }

    // ================== GESTIÓN DE CATEGORÍAS ==================
    @GetMapping("/categorias")
    public String gestionarCategorias(Model model) {
        model.addAttribute("categorias", categoriaService.obtenerTodas());
        return "admin/categorias";
    }

    @PostMapping("/categorias/nueva")
    public String nuevaCategoria(@RequestParam @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre, 
                                 @RequestParam(required = false) String descripcion) {
        Categoria cat = new Categoria();
        cat.setNombre(nombre);
        cat.setDescripcion(descripcion);
        categoriaService.guardar(cat);
        return "redirect:/admin/categorias";
    }

    // ================== ESTADÍSTICAS ==================
    @GetMapping("/estadisticas")
    public String estadisticas(Model model) {
        model.addAttribute("topPublicaciones", publicacionService.obtenerTop5Leidas());
        model.addAttribute("topRedactores", usuarioService.obtenerRedactoresMasActivos());
        model.addAttribute("topCategorias", categoriaService.obtenerCategoriasPopulares());
        return "admin/estadisticas";
    }
}