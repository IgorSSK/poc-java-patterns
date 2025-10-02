package com.translation.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationItem {
    private String id;
    private String originalText;
    private String translatedText;
    private ContentType contentType;
    private String sourceLanguage;
    private String targetLanguage;
    private Map<String, String> metadata;
    private boolean fromCache;
    private boolean hasSensitiveData;
    private Map<String, String> sensitiveDataMasks;
}
