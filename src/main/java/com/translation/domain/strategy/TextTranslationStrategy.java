package com.translation.domain.strategy;

import com.translation.domain.model.TranslationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy para tradução de texto simples usando AWS Translate
 * Melhor para textos curtos e tradução rápida
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TextTranslationStrategy implements TranslationStrategy {

    private final TranslateClient translateClient;

    @Override
    public List<String> translate(List<String> texts, String sourceLang, String targetLang) {
        log.info("Translating {} texts using TextTranslationStrategy", texts.size());
        
        List<String> translations = new ArrayList<>();
        
        for (String text : texts) {
            try {
                TranslateTextRequest request = TranslateTextRequest.builder()
                        .text(text)
                        .sourceLanguageCode(sourceLang)
                        .targetLanguageCode(targetLang)
                        .build();
                
                TranslateTextResponse response = translateClient.translateText(request);
                translations.add(response.translatedText());
                
                log.debug("Translated text: {} -> {}", 
                    text.substring(0, Math.min(50, text.length())),
                    response.translatedText().substring(0, Math.min(50, response.translatedText().length())));
                
            } catch (Exception e) {
                log.error("Error translating text: {}", text, e);
                translations.add(text); // Fallback: retorna texto original
            }
        }
        
        return translations;
    }

    @Override
    public String translateBinary(byte[] content, String sourceLang, String targetLang) {
        // Não suportado para texto simples
        throw new UnsupportedOperationException("Binary translation not supported for TEXT strategy");
    }

    @Override
    public boolean supports(TranslationType type) {
        return type == TranslationType.TEXT;
    }

    @Override
    public TranslationType getType() {
        return TranslationType.TEXT;
    }
}
