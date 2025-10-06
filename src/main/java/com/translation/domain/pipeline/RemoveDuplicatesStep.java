package com.translation.domain.pipeline;

import com.translation.domain.model.TranslationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pipeline Step 1: Remove duplicidades da lista de textos
 * Mantém a ordem original e registra quantas duplicatas foram removidas
 */
@Slf4j
@Component
public class RemoveDuplicatesStep implements PipelineStep {

    @Override
    public TranslationContext execute(TranslationContext context) {
        log.debug("Executing RemoveDuplicatesStep for {} texts", context.getTexts().size());
        
        List<String> originalTexts = context.getTexts();
        int originalSize = originalTexts.size();
        
        // Usa LinkedHashSet para manter ordem e remover duplicatas
        Set<String> uniqueTexts = new LinkedHashSet<>(originalTexts);
        List<String> processedTexts = new ArrayList<>(uniqueTexts);
        
        int duplicatesRemoved = originalSize - processedTexts.size();
        context.setDuplicatesRemoved(duplicatesRemoved);
        context.setProcessedTexts(processedTexts);
        
        log.info("Removed {} duplicates. Remaining texts: {}", duplicatesRemoved, processedTexts.size());
        
        return context;
    }

    @Override
    public String getStepName() {
        return "RemoveDuplicates";
    }
}
