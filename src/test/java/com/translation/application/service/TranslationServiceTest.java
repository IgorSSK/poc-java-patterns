package com.translation.application.service;

import com.translation.domain.model.*;
import com.translation.domain.pipeline.TranslationPipeline;
import com.translation.domain.validator.ValidationChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TranslationService Tests")
class TranslationServiceTest {

    @Mock
    private ValidationChain validationChain;

    @Mock
    private TranslationPipeline translationPipeline;

    @InjectMocks
    private TranslationService translationService;

    private TranslationRequest request;
    private TranslationContext context;

    @BeforeEach
    void setUp() {
        request = TranslationRequest.builder()
                .texts(List.of("Hello world", "Good morning"))
                .sourceLanguage("en")
                .targetLanguage("pt")
                .type(TranslationType.TEXT)
                .useCache(true)
                .removeDuplicates(true)
                .removeSensitiveData(true)
                .build();

        context = TranslationContext.builder()
                .texts(List.of("Hello world", "Good morning"))
                .sourceLanguage("en")
                .targetLanguage("pt")
                .type(TranslationType.TEXT)
                .processedTexts(List.of("Hello world", "Good morning"))
                .translatedTexts(List.of("Olá mundo", "Bom dia"))
                .fromCache(List.of(false, false))
                .hadSensitiveData(List.of(false, false))
                .duplicatesRemoved(0)
                .sensitiveDataRemoved(0)
                .cacheHits(0)
                .cacheMisses(2)
                .startTime(System.currentTimeMillis())
                .build();
    }

    @Test
    @DisplayName("Should translate texts successfully")
    void shouldTranslateTextsSuccessfully() {
        // Given
        doNothing().when(validationChain).validate(any());
        when(translationPipeline.execute(any())).thenReturn(context);
        when(translationPipeline.getStepNames()).thenReturn(
                List.of("RemoveDuplicates", "RemoveSensitiveData", "CacheConsult", 
                       "Translation", "CacheSave", "Log")
        );

        // When
        TranslationResponse response = translationService.translate(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().get(0).getOriginalText()).isEqualTo("Hello world");
        assertThat(response.getResults().get(0).getTranslatedText()).isEqualTo("Olá mundo");
        assertThat(response.getMetadata()).isNotNull();
        assertThat(response.getMetadata().getTotalTexts()).isEqualTo(2);
        assertThat(response.getMetadata().getCacheMisses()).isEqualTo(2);

        verify(validationChain, times(1)).validate(request);
        verify(translationPipeline, times(1)).execute(any());
    }

    @Test
    @DisplayName("Should handle pipeline with cache hits")
    void shouldHandlePipelineWithCacheHits() {
        // Given
        context.setCacheHits(1);
        context.setCacheMisses(1);
        context.setFromCache(List.of(true, false));

        doNothing().when(validationChain).validate(any());
        when(translationPipeline.execute(any())).thenReturn(context);
        when(translationPipeline.getStepNames()).thenReturn(List.of("CacheConsult", "Translation"));

        // When
        TranslationResponse response = translationService.translate(request);

        // Then
        assertThat(response.getMetadata().getCacheHits()).isEqualTo(1);
        assertThat(response.getResults().get(0).isFromCache()).isTrue();
        assertThat(response.getResults().get(1).isFromCache()).isFalse();
    }

    @Test
    @DisplayName("Should track duplicates removal")
    void shouldTrackDuplicatesRemoval() {
        // Given
        context.setDuplicatesRemoved(3);
        context.setTexts(List.of("Hello world", "Good morning", "Hello world", "Hello world", "Good morning"));

        doNothing().when(validationChain).validate(any());
        when(translationPipeline.execute(any())).thenReturn(context);
        when(translationPipeline.getStepNames()).thenReturn(List.of("RemoveDuplicates"));

        // When
        TranslationResponse response = translationService.translate(request);

        // Then
        assertThat(response.getMetadata().getDuplicatesRemoved()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should track sensitive data removal")
    void shouldTrackSensitiveDataRemoval() {
        // Given
        context.setSensitiveDataRemoved(2);
        context.setHadSensitiveData(List.of(true, true));

        doNothing().when(validationChain).validate(any());
        when(translationPipeline.execute(any())).thenReturn(context);
        when(translationPipeline.getStepNames()).thenReturn(List.of("RemoveSensitiveData"));

        // When
        TranslationResponse response = translationService.translate(request);

        // Then
        assertThat(response.getMetadata().getSensitiveDataRemoved()).isEqualTo(2);
        assertThat(response.getResults().get(0).isHadSensitiveData()).isTrue();
        assertThat(response.getResults().get(1).isHadSensitiveData()).isTrue();
    }

    @Test
    @DisplayName("Should include processing time")
    void shouldIncludeProcessingTime() {
        // Given
        doNothing().when(validationChain).validate(any());
        when(translationPipeline.execute(any())).thenReturn(context);
        when(translationPipeline.getStepNames()).thenReturn(List.of("Translation"));

        // When
        TranslationResponse response = translationService.translate(request);

        // Then
        assertThat(response.getMetadata().getProcessingTimeMs()).isGreaterThanOrEqualTo(0);
        assertThat(response.getMetadata().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should include pipeline steps in metadata")
    void shouldIncludePipelineStepsInMetadata() {
        // Given
        List<String> steps = List.of("RemoveDuplicates", "RemoveSensitiveData", 
                                    "CacheConsult", "Translation", "CacheSave", "Log");
        
        doNothing().when(validationChain).validate(any());
        when(translationPipeline.execute(any())).thenReturn(context);
        when(translationPipeline.getStepNames()).thenReturn(steps);

        // When
        TranslationResponse response = translationService.translate(request);

        // Then
        assertThat(response.getMetadata().getPipelineSteps()).containsKey("steps");
        assertThat(response.getMetadata().getPipelineSteps()).containsKey("totalSteps");
        assertThat(response.getMetadata().getPipelineSteps().get("totalSteps")).isEqualTo(6);
    }
}
