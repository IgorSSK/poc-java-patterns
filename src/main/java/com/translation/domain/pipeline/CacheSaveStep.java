package com.translation.domain.pipeline;

import com.translation.domain.model.TranslationContext;
import com.translation.domain.port.CachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Pipeline Step 5: Salva traduções no cache para uso futuro
 * Persiste apenas traduções novas (que não vieram do cache)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheSaveStep implements PipelineStep {

    private final CachePort cachePort;

    @Override
    public TranslationContext execute(TranslationContext context) {
        log.debug("Executing CacheSaveStep for {} texts", context.getProcessedTexts().size());
        
        List<String> processedTexts = context.getProcessedTexts();
        List<String> translatedTexts = context.getTranslatedTexts();
        List<Boolean> fromCache = context.getFromCache();
        
        int savedCount = 0;
        
        for (int i = 0; i < processedTexts.size(); i++) {
            // Salva apenas traduções que não vieram do cache
            if (!fromCache.get(i)) {
                String cacheKey = generateCacheKey(
                    processedTexts.get(i),
                    context.getSourceLanguage(),
                    context.getTargetLanguage()
                );
                
                cachePort.put(cacheKey, translatedTexts.get(i));
                savedCount++;
                
                log.debug("Saved to cache: {}", cacheKey);
            }
        }
        
        log.info("Saved {} new translations to cache", savedCount);
        
        return context;
    }

    @Override
    public String getStepName() {
        return "CacheSave";
    }
    
    private String generateCacheKey(String text, String sourceLang, String targetLang) {
        return String.format("%s:%s:%s", sourceLang, targetLang, text.hashCode());
    }
}
