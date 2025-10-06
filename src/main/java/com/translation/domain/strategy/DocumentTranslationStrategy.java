package com.translation.domain.strategy;

import com.translation.domain.exception.TranslationException;
import com.translation.domain.model.TranslationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy para tradução de documentos (PDF, DOC, DOCX) usando Apache Tika + AWS Translate
 * Extrai texto do documento e traduz o conteúdo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentTranslationStrategy implements TranslationStrategy {

    private final TranslateClient translateClient;

    @Override
    public List<String> translate(List<String> texts, String sourceLang, String targetLang) {
        log.info("Translating {} document texts using DocumentTranslationStrategy", texts.size());
        
        List<String> translations = new ArrayList<>();
        
        for (String text : texts) {
            try {
                // Para documentos, pode fazer chunking para textos muito longos
                String translated = translateLargeText(text, sourceLang, targetLang);
                translations.add(translated);
            } catch (Exception e) {
                log.error("Error translating document text: {}", text, e);
                translations.add(text);
            }
        }
        
        return translations;
    }

    @Override
    public String translateBinary(byte[] content, String sourceLang, String targetLang) {
        log.info("Translating binary document content");
        
        try {
            // Extrai texto do documento usando Apache Tika
            String extractedText = extractTextFromDocument(content);
            
            // Traduz o texto extraído
            return translateLargeText(extractedText, sourceLang, targetLang);
            
        } catch (Exception e) {
            log.error("Error translating binary document", e);
            throw new TranslationException("Failed to translate document", e);
        }
    }

    @Override
    public boolean supports(TranslationType type) {
        return type == TranslationType.DOCUMENT;
    }

    @Override
    public TranslationType getType() {
        return TranslationType.DOCUMENT;
    }
    
    /**
     * Extrai texto de documentos usando Apache Tika
     */
    private String extractTextFromDocument(byte[] content) throws IOException, TikaException, SAXException {
        BodyContentHandler handler = new BodyContentHandler(-1); // -1 = sem limite
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();
        
        parser.parse(new ByteArrayInputStream(content), handler, metadata, context);
        
        log.info("Extracted text from document. Content type: {}", metadata.get("Content-Type"));
        return handler.toString();
    }
    
    /**
     * Traduz textos grandes dividindo em chunks
     * AWS Translate tem limite de 10.000 bytes por requisição
     */
    private String translateLargeText(String text, String sourceLang, String targetLang) {
        final int MAX_CHUNK_SIZE = 5000; // Caracteres por chunk
        
        if (text.length() <= MAX_CHUNK_SIZE) {
            return translateChunk(text, sourceLang, targetLang);
        }
        
        // Divide em chunks
        List<String> chunks = splitIntoChunks(text, MAX_CHUNK_SIZE);
        StringBuilder result = new StringBuilder();
        
        for (String chunk : chunks) {
            result.append(translateChunk(chunk, sourceLang, targetLang));
        }
        
        return result.toString();
    }
    
    private String translateChunk(String text, String sourceLang, String targetLang) {
        try {
            TranslateTextRequest request = TranslateTextRequest.builder()
                    .text(text)
                    .sourceLanguageCode(sourceLang)
                    .targetLanguageCode(targetLang)
                    .build();
            
            TranslateTextResponse response = translateClient.translateText(request);
            return response.translatedText();
            
        } catch (Exception e) {
            log.error("Error translating chunk", e);
            return text; // Fallback
        }
    }
    
    private List<String> splitIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(i + chunkSize, length);
            chunks.add(text.substring(i, end));
        }
        
        return chunks;
    }
}
