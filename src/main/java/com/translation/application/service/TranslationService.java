package com.translation.application.service;

import com.translation.domain.model.*;
import com.translation.domain.pipeline.TranslationPipeline;
import com.translation.domain.validator.ValidationChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Application Service - Orquestra o fluxo de tradução
 * Camada de aplicação na Arquitetura Hexagonal
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final ValidationChain validationChain;
    private final TranslationPipeline translationPipeline;

    /**
     * Método principal para tradução de textos
     */
    public TranslationResponse translate(TranslationRequest request) {
        log.info("Starting translation request for {} texts from {} to {}",
                request.getTexts().size(),
                request.getSourceLanguage(),
                request.getTargetLanguage());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. Validação usando Chain of Responsibility
            validationChain.validate(request);
            
            // 2. Cria contexto para o pipeline
            TranslationContext context = createContext(request);
            
            // 3. Executa o pipeline
            TranslationContext result = translationPipeline.execute(context);
            
            // 4. Constrói resposta
            TranslationResponse response = buildResponse(result, request);
            
            long processingTime = System.currentTimeMillis() - startTime;
            response.getMetadata().setProcessingTimeMs(processingTime);
            response.getMetadata().setTimestamp(LocalDateTime.now());
            
            log.info("Translation completed successfully in {} ms", processingTime);
            
            return response;
            
        } catch (Exception e) {
//            log.error("Error during translation", e);
            throw e;
        }
    }

    /**
     * Tradução de arquivo binário (documento ou imagem)
     */
    public TranslationResponse translateBinary(TranslationRequest request) {
        log.info("Starting binary translation for type {} from {} to {}",
                request.getType(),
                request.getSourceLanguage(),
                request.getTargetLanguage());
        
        // Reutiliza o mesmo fluxo, mas com conteúdo binário
        return translate(request);
    }

    /**
     * Cria o contexto inicial do pipeline
     */
    private TranslationContext createContext(TranslationRequest request) {
        return TranslationContext.builder()
                .texts(new ArrayList<>(request.getTexts()))
                .sourceLanguage(request.getSourceLanguage())
                .targetLanguage(request.getTargetLanguage())
                .type(request.getType())
                .fileContent(request.getFileContent())
                .duplicatesRemoved(0)
                .sensitiveDataRemoved(0)
                .cacheHits(0)
                .cacheMisses(0)
                .startTime(System.currentTimeMillis())
                .processedTexts(new ArrayList<>())
                .translatedTexts(new ArrayList<>())
                .fromCache(new ArrayList<>())
                .hadSensitiveData(new ArrayList<>())
                .build();
    }

    /**
     * Constrói a resposta a partir do contexto processado
     */
    private TranslationResponse buildResponse(TranslationContext context, TranslationRequest request) {
        List<TranslationResponse.TranslationResult> results = new ArrayList<>();
        
        List<String> originalTexts = context.getTexts();
        List<String> translatedTexts = context.getTranslatedTexts();
        List<Boolean> fromCache = context.getFromCache();
        List<Boolean> hadSensitiveData = context.getHadSensitiveData();
        
        for (int i = 0; i < translatedTexts.size(); i++) {
            TranslationResponse.TranslationResult result = TranslationResponse.TranslationResult.builder()
                    .originalText(originalTexts.get(i))
                    .translatedText(translatedTexts.get(i))
                    .sourceLanguage(context.getSourceLanguage())
                    .targetLanguage(context.getTargetLanguage())
                    .fromCache(fromCache.get(i))
                    .hadSensitiveData(i < hadSensitiveData.size() ? hadSensitiveData.get(i) : false)
                    .build();
            
            results.add(result);
        }
        
        // Metadata com estatísticas do pipeline
        Map<String, Object> pipelineSteps = new HashMap<>();
        pipelineSteps.put("steps", translationPipeline.getStepNames());
        pipelineSteps.put("totalSteps", translationPipeline.getStepNames().size());
        
        TranslationResponse.TranslationMetadata metadata = TranslationResponse.TranslationMetadata.builder()
                .totalTexts(originalTexts.size())
                .duplicatesRemoved(context.getDuplicatesRemoved())
                .sensitiveDataRemoved(context.getSensitiveDataRemoved())
                .cacheHits(context.getCacheHits())
                .cacheMisses(context.getCacheMisses())
                .processingTimeMs(0) // Será atualizado depois
                .pipelineSteps(pipelineSteps)
                .build();
        
        return TranslationResponse.builder()
                .results(results)
                .metadata(metadata)
                .build();
    }
}
