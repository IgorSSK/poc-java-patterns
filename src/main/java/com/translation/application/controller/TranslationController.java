package com.translation.application.controller;

import com.translation.application.service.TranslationService;
import com.translation.domain.model.TranslationRequest;
import com.translation.domain.model.TranslationResponse;
import com.translation.domain.model.TranslationType;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller - Camada de apresentação (Adapter de entrada)
 * Recebe requisições HTTP do frontend e delega para o serviço
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/translations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configurar adequadamente em produção
public class TranslationController {

    private final TranslationService translationService;

    /**
     * Endpoint principal para tradução de textos
     * POST /api/v1/translations
     */
    @PostMapping
    @Timed(value = "translation.text", description = "Time taken to translate texts")
    public ResponseEntity<TranslationResponse> translateTexts(
            @Valid @RequestBody TranslationRequest request) {
        
        log.info("Received translation request: {} texts from {} to {}",
                request.getTexts() != null ? request.getTexts().size() : 0,
                request.getSourceLanguage(),
                request.getTargetLanguage());
        
        TranslationResponse response = translationService.translate(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint simplificado para tradução rápida
     * POST /api/v1/translations/quick
     */
    @PostMapping("/quick")
    @Timed(value = "translation.quick", description = "Time taken for quick translation")
    public ResponseEntity<Map<String, Object>> quickTranslate(
            @RequestParam List<String> texts,
            @RequestParam String from,
            @RequestParam String to) {
        
        log.info("Quick translation: {} texts from {} to {}", texts.size(), from, to);
        
        TranslationRequest request = TranslationRequest.builder()
                .texts(texts)
                .sourceLanguage(from)
                .targetLanguage(to)
                .type(TranslationType.TEXT)
                .useCache(true)
                .removeDuplicates(true)
                .removeSensitiveData(true)
                .build();
        
        TranslationResponse response = translationService.translate(request);
        
        // Resposta simplificada
        List<String> translations = response.getResults().stream()
                .map(TranslationResponse.TranslationResult::getTranslatedText)
                .toList();
        
        return ResponseEntity.ok(Map.of(
                "translations", translations,
                "count", translations.size(),
                "cacheHits", response.getMetadata().getCacheHits(),
                "processingTimeMs", response.getMetadata().getProcessingTimeMs()
        ));
    }

    /**
     * Endpoint para tradução de documentos
     * POST /api/v1/translations/document
     */
    @PostMapping(value = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Timed(value = "translation.document", description = "Time taken to translate documents")
    public ResponseEntity<TranslationResponse> translateDocument(
            @RequestParam("file") byte[] file,
            @RequestParam String contentType,
            @RequestParam String from,
            @RequestParam String to) {
        
        log.info("Document translation request: {} bytes, type {}, from {} to {}", 
                file.length, contentType, from, to);
        
        TranslationRequest request = TranslationRequest.builder()
                .texts(List.of()) // Vazio para documentos
                .sourceLanguage(from)
                .targetLanguage(to)
                .type(TranslationType.DOCUMENT)
                .contentType(contentType)
                .fileContent(file)
                .useCache(true)
                .removeDuplicates(false)
                .removeSensitiveData(true)
                .build();
        
        TranslationResponse response = translationService.translateBinary(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para tradução de imagens (OCR + tradução)
     * POST /api/v1/translations/image
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Timed(value = "translation.image", description = "Time taken to translate images")
    public ResponseEntity<TranslationResponse> translateImage(
            @RequestParam("file") byte[] file,
            @RequestParam String from,
            @RequestParam String to) {
        
        log.info("Image translation request: {} bytes from {} to {}", file.length, from, to);
        
        TranslationRequest request = TranslationRequest.builder()
                .texts(List.of())
                .sourceLanguage(from)
                .targetLanguage(to)
                .type(TranslationType.IMAGE)
                .contentType("image/jpeg")
                .fileContent(file)
                .useCache(false) // Imagens geralmente não são cacheadas
                .removeDuplicates(false)
                .removeSensitiveData(true)
                .build();
        
        TranslationResponse response = translationService.translateBinary(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para tradução de HTML
     * POST /api/v1/translations/html
     */
    @PostMapping("/html")
    @Timed(value = "translation.html", description = "Time taken to translate HTML")
    public ResponseEntity<TranslationResponse> translateHtml(
            @RequestBody Map<String, Object> payload) {
        
        @SuppressWarnings("unchecked")
        List<String> htmlTexts = (List<String>) payload.get("htmlTexts");
        String from = (String) payload.get("from");
        String to = (String) payload.get("to");
        
        log.info("HTML translation request: {} texts from {} to {}", htmlTexts.size(), from, to);
        
        TranslationRequest request = TranslationRequest.builder()
                .texts(htmlTexts)
                .sourceLanguage(from)
                .targetLanguage(to)
                .type(TranslationType.HTML)
                .useCache(true)
                .removeDuplicates(true)
                .removeSensitiveData(true)
                .build();
        
        TranslationResponse response = translationService.translate(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "translation-service",
                "version", "1.0.0"
        ));
    }

    /**
     * Retorna idiomas suportados
     */
    @GetMapping("/languages")
    public ResponseEntity<Map<String, Object>> getSupportedLanguages() {
        List<String> languages = List.of("pt", "en", "es", "fr", "de", "it", "ja", "ko", "zh", "ar", "ru");
        
        return ResponseEntity.ok(Map.of(
                "languages", languages,
                "count", languages.size()
        ));
    }

    /**
     * Exception handler
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Error processing request", e);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", e.getClass().getSimpleName(),
                        "message", e.getMessage() != null ? e.getMessage() : "Internal server error",
                        "timestamp", System.currentTimeMillis()
                ));
    }
}
