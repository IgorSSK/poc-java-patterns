package com.translation.domain.validator;

import com.translation.domain.exception.UnsupportedLanguageException;
import com.translation.domain.model.TranslationRequest;
import com.translation.domain.port.TranslationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator 3: Valida idiomas suportados
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LanguageValidator extends AbstractValidationHandler {

    private final TranslationPort translationPort;

    @Override
    protected void doValidate(TranslationRequest request) {
        log.debug("Validating supported languages");
        
        String sourceLang = request.getSourceLanguage().toLowerCase();
        String targetLang = request.getTargetLanguage().toLowerCase();
        
        if (!translationPort.isLanguageSupported(sourceLang)) {
            throw new UnsupportedLanguageException(
                String.format("Source language '%s' is not supported. Supported languages: %s", 
                    sourceLang, translationPort.getSupportedLanguages())
            );
        }
        
        if (!translationPort.isLanguageSupported(targetLang)) {
            throw new UnsupportedLanguageException(
                String.format("Target language '%s' is not supported. Supported languages: %s", 
                    targetLang, translationPort.getSupportedLanguages())
            );
        }
        
        if (sourceLang.equals(targetLang)) {
            throw new UnsupportedLanguageException(
                "Source and target languages must be different"
            );
        }
        
        log.debug("Language validation passed");
    }
}
