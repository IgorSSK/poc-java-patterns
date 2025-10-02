package com.translation.domain.service.pipeline;

import com.translation.domain.model.TranslationRequest;

public interface PipelineStep {
    TranslationRequest execute(TranslationRequest request);
    String getStepName();
}