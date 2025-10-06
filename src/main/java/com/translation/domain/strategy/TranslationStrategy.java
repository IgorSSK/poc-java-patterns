package com.translation.domain.strategy;

import com.translation.domain.model.TranslationType;

import java.util.List;

/**
 * Strategy Pattern - Interface base para diferentes estratégias de tradução
 * Cada tipo de conteúdo (TEXT, DOCUMENT, IMAGE, HTML) tem sua própria estratégia
 */
public interface TranslationStrategy {
    
    /**
     * Traduz uma lista de textos
     */
    List<String> translate(List<String> texts, String sourceLang, String targetLang);
    
    /**
     * Traduz conteúdo binário (documentos/imagens)
     */
    String translateBinary(byte[] content, String sourceLang, String targetLang);
    
    /**
     * Verifica se esta estratégia suporta o tipo especificado
     */
    boolean supports(TranslationType type);
    
    /**
     * Retorna o tipo suportado por esta estratégia
     */
    TranslationType getType();
}
