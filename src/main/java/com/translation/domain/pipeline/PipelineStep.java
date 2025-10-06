package com.translation.domain.pipeline;

import com.translation.domain.model.TranslationContext;

/**
 * Pipeline Pattern - Interface base para todos os passos do pipeline
 * Cada passo processa o contexto e o passa para o próximo
 */
public interface PipelineStep {
    /**
     * Executa o passo do pipeline
     * @param context Contexto da tradução com dados e metadados
     * @return Contexto modificado
     */
    TranslationContext execute(TranslationContext context);
    
    /**
     * Nome do passo para logging e métricas
     */
    String getStepName();
}
