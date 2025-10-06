package com.translation.infrastructure.adapter;

import com.translation.domain.exception.TranslationException;
import com.translation.domain.factory.TranslationStrategyFactory;
import com.translation.domain.model.TranslationType;
import com.translation.domain.port.TranslationPort;
import com.translation.domain.strategy.TranslationStrategy;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter Pattern - Implementa TranslationPort usando Strategy Pattern
 * Adiciona Circuit Breaker e Retry para resiliência
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TranslationAdapter implements TranslationPort {

    private final TranslationStrategyFactory strategyFactory;

    private static final List<String> SUPPORTED_LANGUAGES = List.of(
            "pt", "en", "es", "fr", "de", "it", "ja", "ko", "zh", "ar", "ru"
    );

    @Override
    @CircuitBreaker(name = "translationService", fallbackMethod = "translateFallback")
    @Retry(name = "translationService")
    public List<String> translate(List<String> texts, String sourceLang, String targetLang, TranslationType type) {
        log.info("Translating {} texts from {} to {} using type {}", 
                texts.size(), sourceLang, targetLang, type);
        
        try {
            TranslationStrategy strategy = strategyFactory.getStrategy(type);
            return strategy.translate(texts, sourceLang, targetLang);
        } catch (Exception e) {
            log.error("Error during translation", e);
            throw new TranslationException("Translation failed", e);
        }
    }

    @Override
    @CircuitBreaker(name = "translationService", fallbackMethod = "translateBinaryFallback")
    @Retry(name = "translationService")
    public String translateBinary(byte[] content, String sourceLang, String targetLang, TranslationType type) {
        log.info("Translating binary content from {} to {} using type {}", 
                sourceLang, targetLang, type);
        
        try {
            TranslationStrategy strategy = strategyFactory.getStrategy(type);
            return strategy.translateBinary(content, sourceLang, targetLang);
        } catch (Exception e) {
            log.error("Error during binary translation", e);
            throw new TranslationException("Binary translation failed", e);
        }
    }

    @Override
    public boolean isLanguageSupported(String languageCode) {
        return SUPPORTED_LANGUAGES.contains(languageCode.toLowerCase());
    }

    @Override
    public List<String> getSupportedLanguages() {
        return List.copyOf(SUPPORTED_LANGUAGES);
    }

    // Fallback methods para Circuit Breaker
    private List<String> translateFallback(List<String> texts, String sourceLang, 
                                          String targetLang, TranslationType type, Exception e) {
        log.warn("Translation fallback activated due to: {}", e.getMessage());
        // Retorna textos originais em caso de falha
        return texts;
    }

    private String translateBinaryFallback(byte[] content, String sourceLang, 
                                          String targetLang, TranslationType type, Exception e) {
        log.warn("Binary translation fallback activated due to: {}", e.getMessage());
        return new String(content); // Retorna conteúdo original
    }
}
