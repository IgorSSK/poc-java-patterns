package com.translation.domain.validator;

import com.translation.domain.model.TranslationRequest;

/**
 * Chain of Responsibility Pattern - Interface base para validadores
 * Cada validador processa e passa para o próximo na cadeia
 */
public interface ValidationHandler {
    
    /**
     * Define o próximo validador na cadeia
     */
    ValidationHandler setNext(ValidationHandler next);
    
    /**
     * Executa a validação
     * @param request Requisição a ser validada
     * @throws com.translation.domain.exception.InvalidInputException se validação falhar
     */
    void validate(TranslationRequest request);
}
