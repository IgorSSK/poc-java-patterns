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
    private List<TranslationItem> items;
    private String sourceLanguage;
    private String targetLanguage;
    private ContentType contentType;
    private boolean removeDuplicates;
    private boolean detectSensitiveData;
    private boolean useCache;
    private boolean useDictionary;
}
