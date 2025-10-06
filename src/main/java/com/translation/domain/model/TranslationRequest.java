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
public class TranslationRequest {
    private List<String> texts;
    private String sourceLanguage;
    private String targetLanguage;
    private TranslationType type;
    private String contentType;
    private byte[] fileContent; // Para documentos e imagens
    
    @Builder.Default
    private boolean useCache = true;
    
    @Builder.Default
    private boolean removeDuplicates = true;
    
    @Builder.Default
    private boolean removeSensitiveData = true;
}
