package com.translation.domain.port.output;

import java.util.Map;
import java.util.Optional;

public interface DictionaryRepository {
    Optional<String> findTranslation(String text, String sourceLanguage, String targetLanguage);
    void saveTranslation(String text, String translation, String sourceLanguage, String targetLanguage);
    Map<String, String> findBulkTranslations(Iterable<String> texts, String sourceLanguage, String targetLanguage);
}
