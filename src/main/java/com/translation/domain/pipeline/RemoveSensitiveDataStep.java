package com.translation.domain.pipeline;

import com.translation.domain.model.TranslationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Pipeline Step 2: Remove ou mascara dados sensíveis (LGPD compliance)
 * Remove CPF, CNPJ, emails, telefones, cartões de crédito, etc.
 */
@Slf4j
@Component
public class RemoveSensitiveDataStep implements PipelineStep {

    // Padrões regex para detectar dados sensíveis
    private static final Pattern CPF_PATTERN = Pattern.compile("\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}");
    private static final Pattern CNPJ_PATTERN = Pattern.compile("\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}");
    
    @Override
    public TranslationContext execute(TranslationContext context) {
        log.debug("Executing RemoveSensitiveDataStep for {} texts", context.getProcessedTexts().size());
        
        List<String> processedTexts = context.getProcessedTexts();
        List<String> sanitizedTexts = new ArrayList<>();
        List<Boolean> hadSensitiveData = new ArrayList<>();
        int sensitiveDataCount = 0;
        
        for (String text : processedTexts) {
            String sanitized = text;
            boolean hasSensitive = false;
            
            // Remove/mascara CPF
            if (CPF_PATTERN.matcher(sanitized).find()) {
                sanitized = CPF_PATTERN.matcher(sanitized).replaceAll("[CPF REMOVIDO]");
                hasSensitive = true;
            }
            
            // Remove/mascara CNPJ
            if (CNPJ_PATTERN.matcher(sanitized).find()) {
                sanitized = CNPJ_PATTERN.matcher(sanitized).replaceAll("[CNPJ REMOVIDO]");
                hasSensitive = true;
            }
            
            // Remove/mascara Email
            if (EMAIL_PATTERN.matcher(sanitized).find()) {
                sanitized = EMAIL_PATTERN.matcher(sanitized).replaceAll("[EMAIL REMOVIDO]");
                hasSensitive = true;
            }
            
            // Remove/mascara Telefone
            if (PHONE_PATTERN.matcher(sanitized).find()) {
                sanitized = PHONE_PATTERN.matcher(sanitized).replaceAll("[TELEFONE REMOVIDO]");
                hasSensitive = true;
            }
            
            // Remove/mascara Cartão de Crédito
            if (CREDIT_CARD_PATTERN.matcher(sanitized).find()) {
                sanitized = CREDIT_CARD_PATTERN.matcher(sanitized).replaceAll("[CARTÃO REMOVIDO]");
                hasSensitive = true;
            }
            
            if (hasSensitive) {
                sensitiveDataCount++;
            }
            
            sanitizedTexts.add(sanitized);
            hadSensitiveData.add(hasSensitive);
        }
        
        context.setProcessedTexts(sanitizedTexts);
        context.setHadSensitiveData(hadSensitiveData);
        context.setSensitiveDataRemoved(sensitiveDataCount);
        
        log.info("Removed sensitive data from {} texts", sensitiveDataCount);
        
        return context;
    }

    @Override
    public String getStepName() {
        return "RemoveSensitiveData";
    }
}
