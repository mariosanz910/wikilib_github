package com.tfg.wikilib.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IAService {

    private final RestTemplate restTemplate;

    // Elimina bloques <think>...</think> que devuelve qwen3 por defecto
    private static final Pattern THINK_PATTERN =
            Pattern.compile("<think>[\\s\\S]*?</think>", Pattern.CASE_INSENSITIVE);

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    public IAService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String completarConIA(String titulo, String categoria, String contenidoActual, String tipo) {

        if (contenidoActual == null || contenidoActual.trim().isEmpty()) {
            return "Error: el contenido actual no puede estar vacío.";
        }

        String instruccionLongitud = switch (tipo) {
            case "corto" -> "Genera entre 25 y 40 palabras. Sé muy conciso y directo.";
            case "largo" -> "Genera mínimo 100 palabras. Desarrolla el tema con profundidad, ejemplos y secciones bien diferenciadas.";
            default      -> "Genera entre 50 y 75 palabras. Equilibra profundidad y brevedad.";
        };

        String prompt = """
                Eres un asistente de redacción experto para un blog educativo, que escribe únicamente en Castellano, Español de España en UTF-8 con la ñ.

                El redactor está escribiendo un artículo con estos datos:
                - Título: %s
                - Categoría: %s
                - Contenido actual: %s

                Tu tarea es generar contenido que continúe o complemente lo escrito.

                INSTRUCCIONES DE CONTENIDO:
                1. Analiza el tono, estilo y temática del contenido actual y mantenlos.
                2. No repitas lo que ya está escrito. Solo genera lo que falta o complementa.
                3. %s
                4. Si el tema es técnico, incluye ejemplos concretos o fragmentos de código si aportan valor.
                5. Sé preciso: no inventes datos que no estén respaldados por el contenido actual.

                INSTRUCCIONES DE FORMATO (MUY IMPORTANTE):
                - Usa texto plano. PROHIBIDO usar markdown: sin asteriscos, sin guiones bajos, sin almohadillas, sin backticks.
                - Separa los párrafos con una línea en blanco entre ellos.
                - Para listas usa viñetas con el símbolo • seguido de espacio.
                - Para títulos de sección escríbelos en mayúsculas seguidos de dos puntos en su propia línea.
                - Para código escríbelo directamente sin bloques, precedido de una línea que diga "Ejemplo:".
                - El resultado debe tener estructura visual clara usando solo saltos de línea y el símbolo •.

                RESTRICCIONES:
                - Genera ÚNICAMENTE el texto a añadir.
                - Sin introducciones del tipo "Aquí tienes...", "Claro, te genero...", etc.
                - Sin comillas al inicio o al final del texto generado.
                - Sin etiquetas XML, HTML ni ningún marcado que no sea el indicado arriba.
                - Prohibido generar cosas que se alejen de solo dar la inforamción necesaria tipo:
                    "Como modelo de lenguaje, no puedo..." o "No tengo acceso a...". Si no puedes generar contenido útil, responde con "Error: no se pudo generar contenido relevante.".
                    "FORMATO DEL TEXTO:"
                    "Texto plano sin elementos de markdown."

                MUY IMPORANTE: Si la API devuelve texto con etiquetas <think>, elimínalas y su contenido. Esas etiquetas son usadas por qwen3 para "pensar" pero no deben formar parte del resultado final.
                IMPORTANTE TAMBIÉN: Citar las fuentes de dodne se ha sacado el contenido, solo una frase pequeña, cuanto menos ocupe eso mejor, pero será obligatorio escribirlo, desde luego no entrará en el límite de palabras, si ya generaste 75 de contenido, y aún no pusiste la bibliografía, ponla igual
                """.formatted(titulo, categoria, contenidoActual, instruccionLongitud);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 2048);
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
                        // Doble seguridad: si la API aún devuelve <think>, lo eliminamos aquí
                        content = THINK_PATTERN.matcher(content).replaceAll("").trim();
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