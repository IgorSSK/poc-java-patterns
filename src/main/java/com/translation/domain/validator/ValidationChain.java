package com.translation.domain.validator;

import com.translation.domain.model.TranslationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Chain of Responsibility - Orquestrador da cadeia de validação
 * Configura a ordem dos validadores e executa a cadeia
 */
@Slf4j
@Component
public class ValidationChain {

    private final ValidationHandler firstHandler;

    public ValidationChain(
            RequiredFieldsValidator requiredFieldsValidator,
            SizeValidator sizeValidator,
            LanguageValidator languageValidator,
            FormatValidator formatValidator) {
        
        // Configura a cadeia: Required -> Size -> Language -> Format
        this.firstHandler = requiredFieldsValidator;
        requiredFieldsValidator
            .setNext(sizeValidator)
            .setNext(languageValidator)
            .setNext(formatValidator);
        
        log.info("Validation chain configured with 4 validators");
    }

    /**
     * Executa toda a cadeia de validação
     */
    public void validate(TranslationRequest request) {
        log.info("Starting validation chain");
        firstHandler.validate(request);
        log.info("All validations passed");
    }
}
