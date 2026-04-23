package com.tfg.wikilib.controller;

import com.tfg.wikilib.model.Categoria;
import com.tfg.wikilib.model.Reporte;
import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.repository.CategoriaRepository;
import com.tfg.wikilib.repository.ReporteRepository;
import com.tfg.wikilib.service.PublicacionService;
import com.tfg.wikilib.service.UsuarioService;
import org.springframework.stereotype.Controller;
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
        // Cargar estadísticas básicas para el dashboard si es necesario
        // En la fase 4 se harán estadísticas completas. De momento, enlaces a secciones.
        return "admin/dashboard";
    }

    // ================== GESTIÓN DE USUARIOS ==================
    @GetMapping("/usuarios")
    public String gestionarUsuarios(@RequestParam(required = false) String rol, Model model) {
        if (rol != null && !rol.isEmpty()) {
            // Filtrado muy básico. Si la BBDD crece, mejor hacer un query en el repository.
            model.addAttribute("usuarios", usuarioService.obtenerTodos().stream()
                    .filter(u -> u.getRol().name().equalsIgnoreCase(rol)).toList());
        } else {
            model.addAttribute("usuarios", usuarioService.obtenerTodos());
        }
        model.addAttribute("filtroRol", rol);
        return "admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/toggle-estado")
    public String toggleEstadoUsuario(@PathVariable Long id) {
        // Método en servicio para cambiar estado activo/inactivo (podemos buscarlo y cambiarlo aquí)
        Usuario usuario = usuarioService.obtenerTodos().stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
        if (usuario != null && usuario.getRol() != Usuario.Rol.ADMIN) { // No banear a otros admins
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
    public String resolverReporte(@PathVariable Long id, @RequestParam String accion) {
        Reporte reporte = reporteRepository.findById(id).orElse(null);
        if (reporte != null) {
            if ("ELIMINAR_PUBLICACION".equals(accion)) {
                publicacionService.eliminar(reporte.getPublicacion().getId());
                // Los reportes en cascada deberían borrarse si la BD está configurada,
                // si no, los marcamos como resueltos.
            }
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
}