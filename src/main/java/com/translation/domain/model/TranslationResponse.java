package com.translation.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResponse {
    private List<TranslationResult> results;
    private TranslationMetadata metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranslationResult {
        private String originalText;
        private String translatedText;
        private String sourceLanguage;
        private String targetLanguage;
        private boolean fromCache;
        private boolean hadSensitiveData;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranslationMetadata {
        private int totalTexts;
        private int duplicatesRemoved;
        private int sensitiveDataRemoved;
        private int cacheHits;
        private int cacheMisses;
        private long processingTimeMs;
        private LocalDateTime timestamp;
        private Map<String, Object> pipelineSteps;
    }
}
