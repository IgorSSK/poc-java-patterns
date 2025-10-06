package com.translation.infrastructure.adapter;

import com.translation.domain.port.DictionaryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

/**
 * Adapter Pattern - Repository Pattern para DynamoDB
 * Armazena dicionário de traduções customizadas
 */
@Slf4j
@Component
public class DynamoDbDictionaryAdapter implements DictionaryPort {

    private final DynamoDbEnhancedClient dynamoDbClient;
    private final DynamoDbTable<TranslationEntity> table;

    public DynamoDbDictionaryAdapter(DynamoDbEnhancedClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
        this.table = dynamoDbClient.table("translation-dictionary", 
                TableSchema.fromBean(TranslationEntity.class));
        log.info("DynamoDB Dictionary Adapter initialized");
    }

    @Override
    public Optional<String> findTranslation(String text, String sourceLang, String targetLang) {
        try {
            String pk = generateKey(text, sourceLang, targetLang);
            
            Key key = Key.builder()
                    .partitionValue(pk)
                    .build();
            
            TranslationEntity entity = table.getItem(key);
            
            if (entity != null) {
                log.debug("Dictionary HIT: {}", pk);
                return Optional.of(entity.getTranslation());
            }
            
            log.debug("Dictionary MISS: {}", pk);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error finding translation in dictionary", e);
            return Optional.empty();
        }
    }

    @Override
    public void saveTranslation(String text, String translation, String sourceLang, String targetLang) {
        try {
            TranslationEntity entity = new TranslationEntity();
            entity.setId(generateKey(text, sourceLang, targetLang));
            entity.setOriginalText(text);
            entity.setTranslation(translation);
            entity.setSourceLanguage(sourceLang);
            entity.setTargetLanguage(targetLang);
            entity.setCreatedAt(System.currentTimeMillis());
            
            table.putItem(entity);
            
            log.debug("Saved to dictionary: {}", entity.getId());
            
        } catch (Exception e) {
            log.error("Error saving translation to dictionary", e);
        }
    }

    @Override
    public boolean exists(String text, String sourceLang, String targetLang) {
        return findTranslation(text, sourceLang, targetLang).isPresent();
    }
    
    private String generateKey(String text, String sourceLang, String targetLang) {
        return String.format("%s#%s#%d", sourceLang, targetLang, text.hashCode());
    }

    // Entity class for DynamoDB
    @software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
    public static class TranslationEntity {
        private String id;
        private String originalText;
        private String translation;
        private String sourceLanguage;
        private String targetLanguage;
        private Long createdAt;

        @software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getOriginalText() {
            return originalText;
        }

        public void setOriginalText(String originalText) {
            this.originalText = originalText;
        }

        public String getTranslation() {
            return translation;
        }

        public void setTranslation(String translation) {
            this.translation = translation;
        }

        public String getSourceLanguage() {
            return sourceLanguage;
        }

        public void setSourceLanguage(String sourceLanguage) {
            this.sourceLanguage = sourceLanguage;
        }

        public String getTargetLanguage() {
            return targetLanguage;
        }

        public void setTargetLanguage(String targetLanguage) {
            this.targetLanguage = targetLanguage;
        }

        public Long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Long createdAt) {
            this.createdAt = createdAt;
        }
    }
}
