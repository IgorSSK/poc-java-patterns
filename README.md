# Translation Microservice - Arquitetura Hexagonal

Microsserviço Java 21 com Spring Boot para tradução de textos, documentos, imagens e HTML utilizando AWS Translate e Amazon Bedrock, implementado com **Arquitetura Hexagonal** (Ports & Adapters).

## 🎯 Visão Geral

Este microsserviço processa requisições de tradução de um frontend web através de um **pipeline de 6 passos** que inclui remoção de duplicidades, sanitização de dados sensíveis (LGPD), cache multinível e tradução inteligente usando diferentes estratégias.

## 🏗️ Arquitetura

### Arquitetura Hexagonal (Ports & Adapters)

```
┌─────────────────────────────────────────────────────────────┐
│                    CAMADA DE APRESENTAÇÃO                    │
│  TranslationController (REST API) - Adapter de Entrada      │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                   CAMADA DE APLICAÇÃO                        │
│  TranslationService - Orquestração do fluxo                 │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                    CAMADA DE DOMÍNIO                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Pipeline Pattern (6 passos sequenciais)            │   │
│  │  1. RemoveDuplicatesStep                            │   │
│  │  2. RemoveSensitiveDataStep (LGPD)                  │   │
│  │  3. CacheConsultStep                                │   │
│  │  4. TranslationStep (usa Strategy)                  │   │
│  │  5. CacheSaveStep                                   │   │
│  │  6. LogStep                                         │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Strategy Pattern (4 estratégias)                   │   │
│  │  • TextTranslationStrategy                          │   │
│  │  • DocumentTranslationStrategy                      │   │
│  │  • ImageTranslationStrategy                         │   │
│  │  • HtmlTranslationStrategy                          │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Chain of Responsibility (validações)               │   │
│  │  RequiredFields → Size → Language → Format          │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
│  Ports (Interfaces): TranslationPort, CachePort,            │
│                      DictionaryPort                         │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                 CAMADA DE INFRAESTRUTURA                     │
│  Adapters de Saída:                                         │
│  • TranslationAdapter (AWS Translate + Factory)             │
│  • MultiLevelCacheAdapter (Caffeine L1 + Redis L2)          │
│  • DynamoDbDictionaryAdapter (Repository Pattern)           │
└─────────────────────────────────────────────────────────────┘
```

## 🎨 Padrões de Design Implementados

### 1. **Pipeline Pattern** ⭐
**Justificativa**: Processamento sequencial de dados com múltiplos passos que transformam o contexto.

- **6 passos sequenciais** que processam a requisição:
  1. `RemoveDuplicatesStep` - Remove textos duplicados
  2. `RemoveSensitiveDataStep` - Remove CPF, CNPJ, emails, telefones (LGPD)
  3. `CacheConsultStep` - Consulta cache multinível
  4. `TranslationStep` - Traduz usando estratégia apropriada
  5. `CacheSaveStep` - Persiste traduções novas
  6. `LogStep` - Coleta métricas e logs

### 2. **Strategy Pattern** ⭐
**Justificativa**: Diferentes algoritmos de tradução baseados no tipo de conteúdo.

- **4 estratégias especializadas**:
  - `TextTranslationStrategy` - AWS Translate para texto simples
  - `DocumentTranslationStrategy` - Apache Tika + AWS Translate com chunking
  - `ImageTranslationStrategy` - Amazon Bedrock (Claude 3) para OCR + tradução
  - `HtmlTranslationStrategy` - Jsoup para preservar estrutura HTML

### 3. **Factory Pattern** ⭐
**Justificativa**: Criação dinâmica de estratégias baseada no tipo de conteúdo.

- `TranslationStrategyFactory` seleciona automaticamente a estratégia correta

### 4. **Adapter Pattern** ⭐
**Justificativa**: Integração com serviços externos sem acoplamento ao domínio.

- `TranslationAdapter` - Adapta AWS SDK para o domínio
- `MultiLevelCacheAdapter` - Abstrai Caffeine e Redis
- `DynamoDbDictionaryAdapter` - Abstrai DynamoDB

### 5. **Repository Pattern** ⭐
**Justificativa**: Abstração de acesso a dados (DynamoDB).

- `DictionaryPort` + `DynamoDbDictionaryAdapter` para dicionário customizado

### 6. **Chain of Responsibility** ⭐
**Justificativa**: Validações sequenciais com responsabilidade compartilhada.

- **4 validadores encadeados**:
  1. `RequiredFieldsValidator` - Campos obrigatórios
  2. `SizeValidator` - Limites de tamanho
  3. `LanguageValidator` - Idiomas suportados
  4. `FormatValidator` - Formato e tipo de conteúdo

## 🚀 Features

### Funcionalidades Principais
- ✅ **Tradução Multi-formato**: TEXT, DOCUMENT, IMAGE, HTML
- ✅ **Cache Multinível**: Caffeine (L1 local) + Redis (L2 distribuído)
- ✅ **Dicionário Customizado**: DynamoDB para termos técnicos
- ✅ **Conformidade LGPD**: Remove dados sensíveis automaticamente
- ✅ **Remoção de Duplicidades**: Otimiza custos e performance
- ✅ **Circuit Breaker**: Resilience4j para falhas em AWS
- ✅ **Retry**: 3 tentativas com backoff exponencial
- ✅ **Métricas**: Prometheus + Micrometer
- ✅ **API REST**: Endpoints para todos os tipos de tradução
- ✅ **Swagger/OpenAPI**: Documentação interativa

### Tipos de Tradução Suportados

#### 1. Texto Simples
```bash
POST /api/v1/translations
Content-Type: application/json

{
  "texts": ["Hello world", "Good morning"],
  "sourceLanguage": "en",
  "targetLanguage": "pt",
  "type": "TEXT"
}
```

#### 2. Tradução Rápida
```bash
POST /api/v1/translations/quick?texts=Hello&texts=World&from=en&to=pt
```

#### 3. Documentos (PDF, DOC, DOCX)
```bash
POST /api/v1/translations/document
Content-Type: multipart/form-data

file: document.pdf
contentType: application/pdf
from: en
to: pt
```

#### 4. Imagens (OCR + Tradução)
```bash
POST /api/v1/translations/image
Content-Type: multipart/form-data

file: image.jpg
from: en
to: pt
```

#### 5. HTML
```bash
POST /api/v1/translations/html
Content-Type: application/json

{
  "htmlTexts": ["<h1>Hello</h1>", "<p>World</p>"],
  "from": "en",
  "to": "pt"
}
```

## 🛠️ Tecnologias

### Core
- **Java 21** - Linguagem
- **Spring Boot 3.5.6** - Framework
- **Maven** - Build tool

### AWS Services
- **AWS Translate** - Tradução de texto
- **Amazon Bedrock** - Claude 3 para imagens
- **DynamoDB** - Dicionário de traduções

### Cache & Storage
- **Caffeine** - Cache L1 (local)
- **Redis** - Cache L2 (distribuído)
- **DynamoDB Enhanced Client** - Repository

### Document Processing
- **Apache Tika 2.9.1** - Extração de texto de documentos
- **Jsoup 1.17.2** - Parsing e manipulação de HTML

### Resiliência
- **Resilience4j 2.2.0** - Circuit Breaker, Retry, Time Limiter

### Observabilidade
- **Micrometer** - Métricas
- **Prometheus** - Exposição de métricas
- **Spring Actuator** - Health checks

### Documentação
- **SpringDoc OpenAPI 2.3.0** - Swagger UI

### Outros
- **Lombok** - Redução de boilerplate
- **Jackson** - Serialização JSON
- **SLF4J + Logback** - Logging

## 📦 Estrutura do Projeto

```
src/main/java/com/translation/
├── domain/                          # Domínio (núcleo da hexagonal)
│   ├── model/                       # Entidades e VOs
│   │   ├── TranslationRequest.java
│   │   ├── TranslationResponse.java
│   │   ├── TranslationContext.java
│   │   └── TranslationType.java
│   ├── pipeline/                    # Pipeline Pattern
│   │   ├── PipelineStep.java
│   │   ├── TranslationPipeline.java
│   │   ├── RemoveDuplicatesStep.java
│   │   ├── RemoveSensitiveDataStep.java
│   │   ├── CacheConsultStep.java
│   │   ├── TranslationStep.java
│   │   ├── CacheSaveStep.java
│   │   └── LogStep.java
│   ├── strategy/                    # Strategy Pattern
│   │   ├── TranslationStrategy.java
│   │   ├── TextTranslationStrategy.java
│   │   ├── DocumentTranslationStrategy.java
│   │   ├── ImageTranslationStrategy.java
│   │   └── HtmlTranslationStrategy.java
│   ├── factory/                     # Factory Pattern
│   │   └── TranslationStrategyFactory.java
│   ├── validator/                   # Chain of Responsibility
│   │   ├── ValidationHandler.java
│   │   ├── AbstractValidationHandler.java
│   │   ├── ValidationChain.java
│   │   ├── RequiredFieldsValidator.java
│   │   ├── SizeValidator.java
│   │   ├── LanguageValidator.java
│   │   └── FormatValidator.java
│   ├── port/                        # Ports (interfaces)
│   │   ├── TranslationPort.java
│   │   ├── CachePort.java
│   │   └── DictionaryPort.java
│   └── exception/                   # Exceções do domínio
│       ├── TranslationException.java
│       ├── InvalidInputException.java
│       └── UnsupportedLanguageException.java
├── application/                     # Camada de aplicação
│   ├── service/
│   │   └── TranslationService.java
│   └── controller/
│       └── TranslationController.java
└── infrastructure/                  # Camada de infraestrutura
    ├── adapter/                     # Adapters (implementações)
    │   ├── TranslationAdapter.java
    │   ├── MultiLevelCacheAdapter.java
    │   └── DynamoDbDictionaryAdapter.java
    └── config/                      # Configurações
        ├── AwsConfig.java
        ├── CacheConfig.java
        ├── ResilienceConfig.java
        └── OpenApiConfig.java
```

## 🔧 Configuração

### application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: translation-service
  redis:
    host: localhost
    port: 6379
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=10000,expireAfterWrite=1h

aws:
  region: us-east-1
  translate:
    enabled: true
  bedrock:
    enabled: true
  dynamodb:
    table-name: translation-dictionary

translation:
  cache:
    ttl: 86400
  pipeline:
    enabled: true

resilience4j:
  circuitbreaker:
    instances:
      translationService:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10000

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus

logging:
  level:
    com.translation: DEBUG
    org.springframework: INFO
```

## 🚀 Como Executar

### Pré-requisitos
- Java 21
- Maven 3.8+
- Docker (para Redis)
- AWS Credentials configuradas

### 1. Iniciar Redis
```bash
docker run -d -p 6379:6379 redis:latest
```

### 2. Configurar AWS Credentials
```bash
export AWS_ACCESS_KEY_ID=your_key
export AWS_SECRET_ACCESS_KEY=your_secret
export AWS_REGION=us-east-1
```

### 3. Criar tabela DynamoDB (opcional)
```bash
aws dynamodb create-table \
  --table-name translation-dictionary \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST
```

### 4. Compilar
```bash
./mvnw clean install
```

### 5. Executar
```bash
./mvnw spring-boot:run
```

### 6. Acessar
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Métricas**: http://localhost:8080/actuator/prometheus
- **Health**: http://localhost:8080/actuator/health

## 📊 Métricas e Monitoramento

### Métricas Expostas (Prometheus)
- `translation_text_seconds` - Tempo de tradução de texto
- `translation_quick_seconds` - Tempo de tradução rápida
- `translation_document_seconds` - Tempo de tradução de documento
- `translation_image_seconds` - Tempo de tradução de imagem
- `translation_html_seconds` - Tempo de tradução de HTML

### Health Checks
```bash
GET /actuator/health
```

## 🧪 Exemplo de Resposta

```json
{
  "results": [
    {
      "originalText": "Hello world",
      "translatedText": "Olá mundo",
      "sourceLanguage": "en",
      "targetLanguage": "pt",
      "fromCache": false,
      "hadSensitiveData": false
    }
  ],
  "metadata": {
    "totalTexts": 2,
    "duplicatesRemoved": 1,
    "sensitiveDataRemoved": 0,
    "cacheHits": 0,
    "cacheMisses": 1,
    "processingTimeMs": 234,
    "timestamp": "2025-10-03T10:30:00",
    "pipelineSteps": {
      "steps": ["RemoveDuplicates", "RemoveSensitiveData", "CacheConsult", "Translation", "CacheSave", "Log"],
      "totalSteps": 6
    }
  }
}
```

## 🔒 LGPD / Dados Sensíveis

O serviço remove automaticamente:
- CPF (XXX.XXX.XXX-XX)
- CNPJ (XX.XXX.XXX/XXXX-XX)
- E-mails
- Telefones
- Números de cartão de crédito

## 🎯 Idiomas Suportados

`pt`, `en`, `es`, `fr`, `de`, `it`, `ja`, `ko`, `zh`, `ar`, `ru`

## 📝 Licença

MIT License

## 👤 Autor

**Igor Souza** - [@IgorSSK](https://github.com/IgorSSK)

---

**Arquitetura Hexagonal + Padrões de Design = Código Limpo, Testável e Manutenível** 🚀
