package com.translation.domain.factory;

import com.translation.domain.exception.TranslationException;
import com.translation.domain.model.TranslationType;
import com.translation.domain.strategy.TranslationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory Pattern - Cria e retorna a estratégia correta de tradução
 * baseada no tipo de conteúdo (TEXT, DOCUMENT, IMAGE, HTML)
 */
@Slf4j
@Component
public class TranslationStrategyFactory {

    private final Map<TranslationType, TranslationStrategy> strategies;

    public TranslationStrategyFactory(List<TranslationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        TranslationStrategy::getType,
                        Function.identity()
                ));
        
        log.info("TranslationStrategyFactory initialized with {} strategies: {}", 
                strategies.size(), 
                strategies.keySet());
    }

    /**
     * Retorna a estratégia apropriada para o tipo de conteúdo
     */
    public TranslationStrategy getStrategy(TranslationType type) {
        log.debug("Getting strategy for type: {}", type);
        
        TranslationStrategy strategy = strategies.get(type);
        
        if (strategy == null) {
            log.error("No strategy found for type: {}", type);
            throw new TranslationException("Unsupported translation type: " + type);
        }
        
        log.debug("Selected strategy: {}", strategy.getClass().getSimpleName());
        return strategy;
    }

    /**
     * Verifica se existe estratégia para o tipo
     */
    public boolean hasStrategy(TranslationType type) {
        return strategies.containsKey(type);
    }

    /**
     * Retorna todos os tipos suportados
     */
    public List<TranslationType> getSupportedTypes() {
        return List.copyOf(strategies.keySet());
    }
}
