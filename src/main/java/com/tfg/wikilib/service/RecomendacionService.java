package com.tfg.wikilib.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.repository.PublicacionRepository;

@Service
public class RecomendacionService {

    private final PublicacionRepository publicacionRepository;
    private final RestTemplate restTemplate;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    public RecomendacionService(PublicacionRepository publicacionRepository, RestTemplate restTemplate) {
        this.publicacionRepository = publicacionRepository;
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> obtenerRecomendacion(String preferencia) {
        if (preferencia == null || preferencia.trim().isEmpty()) {
            return Map.of(
                    "exito", false,
                    "error", "Por favor escribe qué te interesa."
            );
        }

        try {
            // Paso 1: Buscar publicaciones relevantes en BD por palabras clave
            List<Publicacion> publicacionesRelevantes = buscarPublicacionesRelevantes(preferencia);

            if (publicacionesRelevantes.isEmpty()) {
                return Map.of(
                        "exito", false,
                        "error", "No se encontraron publicaciones relacionadas con tu búsqueda."
                );
            }

            // Paso 2: Pasar títulos a la IA para que elija las mejores recomendaciones
            String recomendacion = generarRecomendacionConIA(preferencia, publicacionesRelevantes);

            if (recomendacion.startsWith("Error")) {
                return Map.of(
                        "exito", false,
                        "error", recomendacion
                );
            }

            return Map.of(
                    "exito", true,
                    "recomendacion", recomendacion
            );

        } catch (Exception e) {
            return Map.of(
                    "exito", false,
                    "error", "Error al procesar la recomendación: " + e.getMessage()
            );
        }
    }

    private List<Publicacion> buscarPublicacionesRelevantes(String preferencia) {
        // Buscar publicaciones que contengan palabras clave de la preferencia
        String[] palabras = preferencia.toLowerCase().split("\\s+");

        List<Publicacion> todas = publicacionRepository.findAllByOrderByFechaCreacionDesc(
                org.springframework.data.domain.Pageable.unpaged()
        ).getContent();

        return todas.stream()
                .filter(pub -> {
                    String textoCompleto = (pub.getTitulo() + " " + pub.getDescripcion()).toLowerCase();
                    return java.util.Arrays.stream(palabras)
                            .anyMatch(palabra -> textoCompleto.contains(palabra));
                })
                .limit(20) // Máximo 20 publicaciones para no saturar la IA
                .collect(Collectors.toList());
    }

    private String generarRecomendacionConIA(String preferencia, List<Publicacion> publicaciones) {
        // Construir lista de títulos disponibles
        String titulosDisponibles = publicaciones.stream()
                .map(p -> "- " + p.getTitulo())
                .collect(Collectors.joining("\n"));

        String prompt = """
                Eres un asistente recomendador de artículos para un blog educativo.
                
                INSTRUCCIÓN CRÍTICA: NO PIENSES. NO USES <think>. SOLO DEVUELVE EL RESULTADO FINAL.

                El usuario dice que le interesa: "%s"

                Aquí están los títulos de publicaciones disponibles en la base de datos:
                %s

                Tu tarea ÚNICA y FINAL:
                1. Elige los 2-3 títulos más relevantes.
                2. Devuelve EXACTAMENTE este formato:
                
                Títulos de publicaciones recomendadas
                - [Título 1]
                - [Título 2]
                - [Título 3]

                

                REGLAS OBLIGATORIAS:
                - Responde SOLO con el formato anterior. NADA MÁS.
                - Usa los títulos EXACTOS de la lista (no los modifiques).
                - PROHIBIDO agregar explicaciones, razonamientos o análisis.
                - PROHIBIDO usar <think>, tags XML, o cualquier marcado.
                - Si no hay títulos relevantes: "Error: no se encontraron artículos relacionados."
                """.formatted(preferencia, titulosDisponibles);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 200);
            requestBody.put("messages", List.of(
                    Map.of("role", "user", "content", prompt)
            ));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices =
                        (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        String content = (String) message.get("content");
                        
                        // Eliminar bloques <think>...</think> de todas las formas posibles
                        content = content.replaceAll("(?s)<think>.*?</think>", "");
                        content = content.replaceAll("(?s)<!--.*?-->", "");
                        content = content.trim();
                        
                        // Si quedó vacío, retornar error
                        if (content.isEmpty()) {
                            return "Error: no se pudo procesar la recomendación.";
                        }
                        
                        return content;
                    }
                }
            }

            return "Error: no se pudo obtener una respuesta válida de la IA.";

        } catch (Exception e) {
            return "Error al conectar con la IA: " + e.getMessage();
        }
    }
}