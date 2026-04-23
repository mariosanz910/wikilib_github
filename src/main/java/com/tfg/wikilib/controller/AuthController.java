package com.tfg.wikilib.controller;

import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Página de login
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // Página de registro
    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/registro";
    }

    // Procesar registro
    @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute Usuario usuario,
                                   Model model) {
        try {
            usuarioService.registrarUsuario(usuario);
            // Redirigir al login después del registro
            return "redirect:/login";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/registro";
        }
    }

}