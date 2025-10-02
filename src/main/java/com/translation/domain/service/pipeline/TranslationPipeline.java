package com.translation.domain.service.pipeline;

import com.translation.domain.model.TranslationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TranslationPipeline {
    
    private final List<PipelineStep> steps = new ArrayList<>();
    
    public TranslationPipeline addStep(PipelineStep step) {
        this.steps.add(step);
        return this;
    }
    
    public TranslationRequest execute(TranslationRequest request) {
        log.info("Starting translation pipeline with {} steps", steps.size());
        
        TranslationRequest currentRequest = request;
        
        for (PipelineStep step : steps) {
            long startTime = System.currentTimeMillis();
            log.debug("Executing step: {}", step.getStepName());
            
            currentRequest = step.execute(currentRequest);
            
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Step {} completed in {}ms", step.getStepName(), duration);
        }
        
        log.info("Translation pipeline completed");
        return currentRequest;
    }
}