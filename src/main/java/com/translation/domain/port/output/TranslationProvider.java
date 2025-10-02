package com.translation.domain.port.output;

import com.translation.domain.model.ContentType;
import com.translation.domain.model.TranslationItem;

import java.util.List;

public interface TranslationProvider {
    List<TranslationItem> translate(List<TranslationItem> items, String sourceLanguage, String targetLanguage);
    boolean supports(ContentType contentType);
}
