package com.translation.domain.validator;

import com.translation.domain.exception.InvalidInputException;
import com.translation.domain.model.TranslationRequest;
import com.translation.domain.model.TranslationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validator 4: Valida formato e tipo de conteúdo
 */
@Slf4j
@Component
public class FormatValidator extends AbstractValidationHandler {

    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]+>");

    @Override
    protected void doValidate(TranslationRequest request) {
        log.debug("Validating format and content type");
        
        TranslationType type = request.getType();
        
        // Valida se o tipo corresponde ao conteúdo
        switch (type) {
            case DOCUMENT:
            case IMAGE:
                if (request.getFileContent() == null || request.getFileContent().length == 0) {
                    throw new InvalidInputException(
                        String.format("File content is required for type %s", type)
                    );
                }
                if (request.getContentType() == null || request.getContentType().isBlank()) {
                    throw new InvalidInputException(
                        "Content type is required for binary content"
                    );
                }
                break;
                
            case HTML:
                // Valida se realmente contém HTML
                boolean hasHtml = request.getTexts().stream()
                    .anyMatch(text -> HTML_PATTERN.matcher(text).find());
                
                if (!hasHtml) {
                    log.warn("Type is HTML but no HTML tags found in texts");
                }
                break;
                
            case TEXT:
                // Texto simples não tem restrições especiais
                break;
        }
        
        log.debug("Format validation passed");
    }
}
