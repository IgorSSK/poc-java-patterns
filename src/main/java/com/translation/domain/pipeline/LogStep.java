package com.translation.domain.pipeline;

import com.translation.domain.model.TranslationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Pipeline Step 6: Log final com estatísticas e métricas do processamento
 * Último passo do pipeline - coleta métricas para monitoramento
 */
@Slf4j
@Component
public class LogStep implements PipelineStep {

    @Override
    public TranslationContext execute(TranslationContext context) {
        log.debug("Executing LogStep");
        
        long processingTime = System.currentTimeMillis() - context.getStartTime();
        
        log.info("=== Translation Pipeline Completed ===");
        log.info("Total texts received: {}", context.getTexts().size());
        log.info("Duplicates removed: {}", context.getDuplicatesRemoved());
        log.info("Sensitive data removed from: {} texts", context.getSensitiveDataRemoved());
        log.info("Cache hits: {}", context.getCacheHits());
        log.info("Cache misses: {}", context.getCacheMisses());
        log.info("Cache hit rate: {}%", 
            context.getCacheHits() > 0 
                ? (context.getCacheHits() * 100.0 / (context.getCacheHits() + context.getCacheMisses())) 
                : 0);
        log.info("Texts translated: {}", context.getProcessedTexts().size());
        log.info("Total processing time: {} ms", processingTime);
        log.info("Average time per text: {} ms", 
            context.getProcessedTexts().size() > 0 
                ? processingTime / context.getProcessedTexts().size() 
                : 0);
        log.info("====================================");
        
        return context;
    }

    @Override
    public String getStepName() {
        return "Log";
    }
}
