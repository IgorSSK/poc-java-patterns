package com.translation.domain.validator;

import com.translation.domain.exception.InvalidInputException;
import com.translation.domain.model.TranslationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator 2: Valida tamanho dos textos e limites
 */
@Slf4j
@Component
public class SizeValidator extends AbstractValidationHandler {

    private static final int MAX_TEXTS = 1000;
    private static final int MAX_TEXT_LENGTH = 10000;
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    @Override
    protected void doValidate(TranslationRequest request) {
        log.debug("Validating sizes and limits");
        
        // Valida número de textos
        if (request.getTexts().size() > MAX_TEXTS) {
            throw new InvalidInputException(
                String.format("Maximum %d texts allowed, received %d", MAX_TEXTS, request.getTexts().size())
            );
        }
        
        // Valida tamanho de cada texto
        for (int i = 0; i < request.getTexts().size(); i++) {
            String text = request.getTexts().get(i);
            if (text == null) {
                throw new InvalidInputException("Text at index " + i + " is null");
            }
            if (text.length() > MAX_TEXT_LENGTH) {
                throw new InvalidInputException(
                    String.format("Text at index %d exceeds maximum length of %d characters", i, MAX_TEXT_LENGTH)
                );
            }
        }
        
        // Valida tamanho de arquivo se presente
        if (request.getFileContent() != null && request.getFileContent().length > MAX_FILE_SIZE) {
            throw new InvalidInputException(
                String.format("File size exceeds maximum of %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }
        
        log.debug("Size validation passed");
    }
}
