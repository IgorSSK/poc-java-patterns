package com.translation.domain.port;

import com.translation.domain.model.TranslationType;

import java.util.List;

/**
 * Port (Hexagonal Architecture) - Interface para serviços de tradução
 * Permite trocar implementações sem afetar o domínio
 */
public interface TranslationPort {
    
    List<String> translate(List<String> texts, String sourceLang, String targetLang, TranslationType type);
    
    String translateBinary(byte[] content, String sourceLang, String targetLang, TranslationType type);
    
    boolean isLanguageSupported(String languageCode);
    
    List<String> getSupportedLanguages();
}
