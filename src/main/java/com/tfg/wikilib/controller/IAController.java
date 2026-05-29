package com.tfg.wikilib.controller;

import com.tfg.wikilib.service.IAService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ia")
public class IAController {

    private final IAService iaService;

    public IAController(IAService iaService) {
        this.iaService = iaService;
    }

    @PostMapping("/completar-publicacion")
    public ResponseEntity<Map<String, Object>> completarPublicacion(
            @RequestBody Map<String, String> body) {

        String titulo          = body.getOrDefault("titulo", "").trim();
        String categoria       = body.getOrDefault("categoria", "General").trim();
        String contenidoActual = body.getOrDefault("contenidoActual", "").trim();
        String tipo            = body.getOrDefault("tipo", "medio").trim();

        if (contenidoActual.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "exito", false,
                    "error", "El contenido actual no puede estar vacío. Escribe algo primero para que la IA pueda ayudarte."
            ));
        }

        if (titulo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "exito", false,
                    "error", "El título no puede estar vacío."
            ));
        }

        String contenidoGenerado = iaService.completarConIA(titulo, categoria, contenidoActual, tipo);

        if (contenidoGenerado.startsWith("Error")) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "exito", false,
                    "error", contenidoGenerado
            ));
        }

        return ResponseEntity.ok(Map.of(
                "exito", true,
                "contenidoGenerado", contenidoGenerado
        ));
    }
}