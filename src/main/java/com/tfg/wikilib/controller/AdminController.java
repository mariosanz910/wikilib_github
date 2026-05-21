package com.tfg.wikilib.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tfg.wikilib.model.Categoria;
import com.tfg.wikilib.model.Reporte;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.service.CategoriaService;
import com.tfg.wikilib.service.PublicacionService;
import com.tfg.wikilib.service.ReporteService;
import com.tfg.wikilib.service.UsuarioService;

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

    // Constante: 15 resultados por página
    private static final int TAMAÑO_PAGINA = 15;

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
    public String gestionarReportes(@RequestParam(required = false) String buscar,
                                    @RequestParam(defaultValue = "0") int page,
                                    Model model) {

        // Validar que page sea >= 0
        if (page < 0) {
            page = 0;
        }

        // Crear objeto Pageable
        Pageable pageable = PageRequest.of(page, TAMAÑO_PAGINA);

        // Obtener reportes según búsqueda
        Page<Reporte> pageReportes;

        if (buscar != null && !buscar.isBlank()) {
            pageReportes = reporteService.buscarPorTituloPublicacion(buscar, pageable);
            model.addAttribute("buscar", buscar);
        } else {
            pageReportes = reporteService.obtenerPendientesPaginados(pageable);
        }

        // Enviar datos a la vista
        model.addAttribute("page", pageReportes);
        model.addAttribute("reportes", pageReportes.getContent());
        model.addAttribute("totalPages", pageReportes.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("hasPrevious", pageReportes.hasPrevious());
        model.addAttribute("hasNext", pageReportes.hasNext());
        model.addAttribute("totalElements", pageReportes.getTotalElements());

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