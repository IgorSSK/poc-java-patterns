package com.translation.domain.pipeline;

import com.translation.domain.model.TranslationContext;
import com.translation.domain.port.TranslationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline Step 4: Traduz textos que não estavam em cache
 * Usa Strategy Pattern para selecionar a estratégia correta de tradução
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TranslationStep implements PipelineStep {

    private final TranslationPort translationPort;

    @Override
    public TranslationContext execute(TranslationContext context) {
        log.debug("Executing TranslationStep for {} texts", context.getProcessedTexts().size());
        
        List<String> processedTexts = context.getProcessedTexts();
        List<String> translatedTexts = context.getTranslatedTexts();
        List<Boolean> fromCache = context.getFromCache();
        
        List<String> textsToTranslate = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        
        // Identifica textos que precisam ser traduzidos (não estavam em cache)
        for (int i = 0; i < translatedTexts.size(); i++) {
            if (translatedTexts.get(i) == null) {
                textsToTranslate.add(processedTexts.get(i));
                indices.add(i);
            }
        }
        
        if (!textsToTranslate.isEmpty()) {
            log.info("Translating {} texts that were not in cache", textsToTranslate.size());
            
            // Traduz em lote usando a porta de tradução
            List<String> translations = translationPort.translate(
                textsToTranslate,
                context.getSourceLanguage(),
                context.getTargetLanguage(),
                context.getType()
            );
            
            // Atualiza o contexto com as traduções
            for (int i = 0; i < indices.size(); i++) {
                int idx = indices.get(i);
                translatedTexts.set(idx, translations.get(i));
            }
            
            log.info("Successfully translated {} texts", translations.size());
        } else {
            log.info("All texts were found in cache, no translation needed");
        }
        
        context.setTranslatedTexts(translatedTexts);
        
        return context;
    }

    @Override
    public String getStepName() {
        return "Translation";
    }
}
