package com.translation.domain.port;

import java.util.Optional;

/**
 * Port (Hexagonal Architecture) - Interface para repositório de dicionário
 * Permite armazenar traduções customizadas e termos técnicos
 */
public interface DictionaryPort {
    
    Optional<String> findTranslation(String text, String sourceLang, String targetLang);
    
    void saveTranslation(String text, String translation, String sourceLang, String targetLang);
    
    boolean exists(String text, String sourceLang, String targetLang);
}
