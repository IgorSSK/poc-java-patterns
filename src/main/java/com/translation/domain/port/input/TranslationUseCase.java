package com.translation.domain.port.input;

import com.translation.domain.model.TranslationRequest;
import com.translation.domain.model.TranslationResponse;

public interface TranslationUseCase {
    TranslationResponse translate(TranslationRequest request);
}
