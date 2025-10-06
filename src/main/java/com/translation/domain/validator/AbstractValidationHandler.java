package com.translation.domain.validator;

import com.translation.domain.model.TranslationRequest;

/**
 * Base abstrata para handlers de validação
 * Implementa a lógica de encadeamento
 */
public abstract class AbstractValidationHandler implements ValidationHandler {
    
    private ValidationHandler next;

    @Override
    public ValidationHandler setNext(ValidationHandler next) {
        this.next = next;
        return next;
    }

    @Override
    public void validate(TranslationRequest request) {
        doValidate(request);
        
        if (next != null) {
            next.validate(request);
        }
    }
    
    /**
     * Implementado por cada validador concreto
     */
    protected abstract void doValidate(TranslationRequest request);
}
