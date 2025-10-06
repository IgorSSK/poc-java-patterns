package com.translation.domain.pipeline;

import com.translation.domain.model.TranslationContext;
import com.translation.domain.port.CachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline Step 3: Consulta cache para traduções já realizadas
 * Usa cache multinível (Caffeine + Redis) para otimizar performance
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheConsultStep implements PipelineStep {

    private final CachePort cachePort;

    @Override
    public TranslationContext execute(TranslationContext context) {
        log.debug("Executing CacheConsultStep for {} texts", context.getProcessedTexts().size());
        
        List<String> processedTexts = context.getProcessedTexts();
        List<String> translatedTexts = new ArrayList<>();
        List<Boolean> fromCache = new ArrayList<>();
        int cacheHits = 0;
        int cacheMisses = 0;
        
        for (String text : processedTexts) {
            String cacheKey = generateCacheKey(text, context.getSourceLanguage(), context.getTargetLanguage());
            String cached = cachePort.get(cacheKey);
            
            if (cached != null) {
                translatedTexts.add(cached);
                fromCache.add(true);
                cacheHits++;
                log.debug("Cache HIT for text: {}", text.substring(0, Math.min(50, text.length())));
            } else {
                translatedTexts.add(null); // Será traduzido no próximo step
                fromCache.add(false);
                cacheMisses++;
                log.debug("Cache MISS for text: {}", text.substring(0, Math.min(50, text.length())));
            }
        }
        
        context.setTranslatedTexts(translatedTexts);
        context.setFromCache(fromCache);
        context.setCacheHits(cacheHits);
        context.setCacheMisses(cacheMisses);
        
        log.info("Cache stats - Hits: {}, Misses: {}, Hit rate: {}%", 
                cacheHits, cacheMisses, 
                cacheHits > 0 ? (cacheHits * 100.0 / (cacheHits + cacheMisses)) : 0);
        
        return context;
    }

    @Override
    public String getStepName() {
        return "CacheConsult";
    }
    
    private String generateCacheKey(String text, String sourceLang, String targetLang) {
        return String.format("%s:%s:%s", sourceLang, targetLang, text.hashCode());
    }
}
