package com.translation.domain.pipeline;

import com.translation.domain.model.TranslationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Pipeline Pattern - Orquestrador que executa os passos sequencialmente
 * Implementa o padrão Pipeline para processamento sequencial de dados
 */
@Slf4j
@Component
public class TranslationPipeline {

    private final List<PipelineStep> steps;

    public TranslationPipeline(
            RemoveDuplicatesStep removeDuplicatesStep,
            RemoveSensitiveDataStep removeSensitiveDataStep,
            CacheConsultStep cacheConsultStep,
            TranslationStep translationStep,
            CacheSaveStep cacheSaveStep,
            LogStep logStep) {
        
        // Define a ordem dos passos do pipeline
        this.steps = List.of(
            removeDuplicatesStep,      // 1. Remove duplicidades
            removeSensitiveDataStep,   // 2. Remove dados sensíveis (LGPD)
            cacheConsultStep,          // 3. Consulta cache
            translationStep,           // 4. Traduz (usando Strategy Pattern)
            cacheSaveStep,             // 5. Salva no cache
            logStep                    // 6. Log e métricas
        );
        
        log.info("Translation Pipeline initialized with {} steps", steps.size());
    }

    /**
     * Executa o pipeline completo
     */
    public TranslationContext execute(TranslationContext context) {
        log.info("Starting Translation Pipeline with {} texts", context.getTexts().size());
        context.setStartTime(System.currentTimeMillis());
        
        TranslationContext currentContext = context;
        
        for (PipelineStep step : steps) {
            try {
                log.debug("Executing pipeline step: {}", step.getStepName());
                currentContext = step.execute(currentContext);
            } catch (Exception e) {
                log.error("Error executing pipeline step: {}", step.getStepName(), e);
                throw new RuntimeException("Pipeline failed at step: " + step.getStepName(), e);
            }
        }
        
        log.info("Translation Pipeline completed successfully");
        return currentContext;
    }
    
    /**
     * Retorna a lista de passos configurados
     */
    public List<String> getStepNames() {
        return steps.stream()
                .map(PipelineStep::getStepName)
                .toList();
    }
}
