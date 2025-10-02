package com.translation.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResponse {
    private List<TranslationItem> items;
    private int totalItems;
    private int cacheHits;
    private int dictionaryHits;
    private int translatedItems;
    private LocalDateTime processedAt;
    private long processingTimeMs;
}
