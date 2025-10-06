package com.translation.domain.strategy;

import com.translation.domain.model.TranslationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy para tradução de HTML usando Jsoup + AWS Translate
 * Preserva a estrutura HTML, traduzindo apenas o conteúdo textual
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HtmlTranslationStrategy implements TranslationStrategy {

    private final TranslateClient translateClient;

    @Override
    public List<String> translate(List<String> htmlTexts, String sourceLang, String targetLang) {
        log.info("Translating {} HTML texts using HtmlTranslationStrategy", htmlTexts.size());
        
        List<String> translations = new ArrayList<>();
        
        for (String html : htmlTexts) {
            try {
                String translatedHtml = translateHtml(html, sourceLang, targetLang);
                translations.add(translatedHtml);
            } catch (Exception e) {
                log.error("Error translating HTML", e);
                translations.add(html); // Fallback
            }
        }
        
        return translations;
    }

    @Override
    public String translateBinary(byte[] content, String sourceLang, String targetLang) {
        String html = new String(content);
        return translateHtml(html, sourceLang, targetLang);
    }

    @Override
    public boolean supports(TranslationType type) {
        return type == TranslationType.HTML;
    }

    @Override
    public TranslationType getType() {
        return TranslationType.HTML;
    }
    
    /**
     * Traduz HTML preservando a estrutura
     */
    private String translateHtml(String html, String sourceLang, String targetLang) {
        Document doc = Jsoup.parse(html);
        
        // Traduz title
        if (doc.title() != null && !doc.title().isEmpty()) {
            String translatedTitle = translateText(doc.title(), sourceLang, targetLang);
            doc.title(translatedTitle);
        }
        
        // Traduz meta descriptions
        Elements metaDescriptions = doc.select("meta[name=description]");
        for (Element meta : metaDescriptions) {
            String content = meta.attr("content");
            if (content != null && !content.isEmpty()) {
                String translatedContent = translateText(content, sourceLang, targetLang);
                meta.attr("content", translatedContent);
            }
        }
        
        // Traduz todo texto visível, preservando HTML
        translateTextNodes(doc.body(), sourceLang, targetLang);
        
        // Traduz atributos alt de imagens
        Elements images = doc.select("img[alt]");
        for (Element img : images) {
            String alt = img.attr("alt");
            if (alt != null && !alt.isEmpty()) {
                String translatedAlt = translateText(alt, sourceLang, targetLang);
                img.attr("alt", translatedAlt);
            }
        }
        
        // Traduz placeholders de inputs
        Elements inputs = doc.select("input[placeholder], textarea[placeholder]");
        for (Element input : inputs) {
            String placeholder = input.attr("placeholder");
            if (placeholder != null && !placeholder.isEmpty()) {
                String translatedPlaceholder = translateText(placeholder, sourceLang, targetLang);
                input.attr("placeholder", translatedPlaceholder);
            }
        }
        
        log.info("HTML translation completed");
        return doc.html();
    }
    
    /**
     * Traduz recursivamente todos os text nodes
     */
    private void translateTextNodes(Element element, String sourceLang, String targetLang) {
        if (element == null) return;
        
        // Não traduz scripts, styles, etc
        if (element.tagName().equalsIgnoreCase("script") || 
            element.tagName().equalsIgnoreCase("style")) {
            return;
        }
        
        // Traduz text nodes diretos
        for (TextNode textNode : element.textNodes()) {
            String text = textNode.text().trim();
            if (!text.isEmpty()) {
                String translated = translateText(text, sourceLang, targetLang);
                textNode.text(translated);
            }
        }
        
        // Recursivamente processa filhos
        for (Element child : element.children()) {
            translateTextNodes(child, sourceLang, targetLang);
        }
    }
    
    /**
     * Traduz um único texto usando AWS Translate
     */
    private String translateText(String text, String sourceLang, String targetLang) {
        try {
            TranslateTextRequest request = TranslateTextRequest.builder()
                    .text(text)
                    .sourceLanguageCode(sourceLang)
                    .targetLanguageCode(targetLang)
                    .build();
            
            TranslateTextResponse response = translateClient.translateText(request);
            return response.translatedText();
            
        } catch (Exception e) {
            log.error("Error translating text: {}", text, e);
            return text; // Fallback
        }
    }
}
