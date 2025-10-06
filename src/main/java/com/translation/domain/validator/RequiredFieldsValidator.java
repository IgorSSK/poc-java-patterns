package com.translation.domain.validator;

import com.translation.domain.exception.InvalidInputException;
import com.translation.domain.model.TranslationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator 1: Valida campos obrigatórios
 */
@Slf4j
@Component
public class RequiredFieldsValidator extends AbstractValidationHandler {

    @Override
    protected void doValidate(TranslationRequest request) {
        log.debug("Validating required fields");
        
        if (request == null) {
            throw new InvalidInputException("Request cannot be null");
        }
        
        if (request.getTexts() == null || request.getTexts().isEmpty()) {
            throw new InvalidInputException("Texts list cannot be null or empty");
        }
        
        if (request.getSourceLanguage() == null || request.getSourceLanguage().isBlank()) {
            throw new InvalidInputException("Source language is required");
        }
        
        if (request.getTargetLanguage() == null || request.getTargetLanguage().isBlank()) {
            throw new InvalidInputException("Target language is required");
        }
        
        if (request.getType() == null) {
            throw new InvalidInputException("Translation type is required");
        }
        
        log.debug("Required fields validation passed");
    }
}
