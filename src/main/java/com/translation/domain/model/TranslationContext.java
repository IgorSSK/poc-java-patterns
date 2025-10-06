package com.translation.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationContext {
    private List<String> texts;
    private String sourceLanguage;
    private String targetLanguage;
    private TranslationType type;
    private byte[] fileContent;
    
    // Metadata do pipeline
    private int duplicatesRemoved;
    private int sensitiveDataRemoved;
    private int cacheHits;
    private int cacheMisses;
    private long startTime;
    
    // Resultados intermediários
    private List<String> processedTexts;
    private List<String> translatedTexts;
    private List<Boolean> fromCache;
    private List<Boolean> hadSensitiveData;
}
