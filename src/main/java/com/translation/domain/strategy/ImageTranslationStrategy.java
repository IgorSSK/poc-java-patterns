package com.translation.domain.strategy;

import com.translation.domain.exception.TranslationException;
import com.translation.domain.model.TranslationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Strategy para tradução de imagens usando Amazon Bedrock (Claude 3 com visão)
 * Usa modelo multimodal para OCR + tradução de texto em imagens
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageTranslationStrategy implements TranslationStrategy {

    private final BedrockRuntimeClient bedrockClient;
    private static final String MODEL_ID = "anthropic.claude-3-sonnet-20240229-v1:0";

    @Override
    public List<String> translate(List<String> texts, String sourceLang, String targetLang) {
        // Para imagens, normalmente recebemos base64 ou precisamos processar binário
        log.warn("Image translation called with text list - not ideal for this strategy");
        return new ArrayList<>(texts); // Retorna original
    }

    @Override
    public String translateBinary(byte[] content, String sourceLang, String targetLang) {
        log.info("Translating image content using Amazon Bedrock");
        
        try {
            String base64Image = Base64.getEncoder().encodeToString(content);
            
            // Prompt para Claude extrair e traduzir texto da imagem
            String prompt = String.format(
                "Extract all text from this image and translate it from %s to %s. " +
                "Return only the translated text, preserving the original formatting as much as possible.",
                sourceLang, targetLang
            );
            
            String requestBody = String.format("""
                {
                    "anthropic_version": "bedrock-2023-05-31",
                    "max_tokens": 4096,
                    "messages": [
                        {
                            "role": "user",
                            "content": [
                                {
                                    "type": "image",
                                    "source": {
                                        "type": "base64",
                                        "media_type": "image/jpeg",
                                        "data": "%s"
                                    }
                                },
                                {
                                    "type": "text",
                                    "text": "%s"
                                }
                            ]
                        }
                    ]
                }
                """, base64Image, prompt);
            
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(MODEL_ID)
                    .body(SdkBytes.fromUtf8String(requestBody))
                    .build();
            
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();
            
            // Parse resposta (simplificado - em produção usar Jackson)
            String translatedText = extractTextFromResponse(responseBody);
            
            log.info("Image translation completed successfully");
            return translatedText;
            
        } catch (Exception e) {
            log.error("Error translating image", e);
            throw new TranslationException("Failed to translate image", e);
        }
    }

    @Override
    public boolean supports(TranslationType type) {
        return type == TranslationType.IMAGE;
    }

    @Override
    public TranslationType getType() {
        return TranslationType.IMAGE;
    }
    
    private String extractTextFromResponse(String responseBody) {
        // Simplificado - em produção usar Jackson para parsing JSON
        try {
            int startIdx = responseBody.indexOf("\"text\":\"") + 8;
            int endIdx = responseBody.indexOf("\"", startIdx);
            return responseBody.substring(startIdx, endIdx)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
        } catch (Exception e) {
            log.error("Error parsing Bedrock response", e);
            return "";
        }
    }
}
