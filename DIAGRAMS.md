# Diagramas Visuais - Translation Microservice

## 📊 Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              FRONTEND WEB                                    │
│                         (React, Angular, Vue, etc)                           │
└────────────────────────────────────┬────────────────────────────────────────┘
                                     │ HTTP/REST
                                     │
┌────────────────────────────────────▼────────────────────────────────────────┐
│                          TRANSLATION SERVICE                                 │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │                    PRESENTATION LAYER                              │    │
│  │  ╔══════════════════════════════════════════════════════════╗     │    │
│  │  ║         TranslationController (REST API)                 ║     │    │
│  │  ║  • POST /api/v1/translations                            ║     │    │
│  │  ║  • POST /api/v1/translations/quick                      ║     │    │
│  │  ║  • POST /api/v1/translations/document                   ║     │    │
│  │  ║  • POST /api/v1/translations/image                      ║     │    │
│  │  ║  • POST /api/v1/translations/html                       ║     │    │
│  │  ║  • GET  /api/v1/translations/languages                  ║     │    │
│  │  ╚══════════════════════════════════════════════════════════╝     │    │
│  └─────────────────────────────────┬──────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────▼──────────────────────────────────┐    │
│  │                    APPLICATION LAYER                               │    │
│  │  ╔══════════════════════════════════════════════════════════╗     │    │
│  │  ║            TranslationService                            ║     │    │
│  │  ║  Orquestra: Validação → Pipeline → Resposta            ║     │    │
│  │  ╚══════════════════════════════════════════════════════════╝     │    │
│  └─────────────────────────────────┬──────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────▼──────────────────────────────────┐    │
│  │                      DOMAIN LAYER                                  │    │
│  │                                                                     │    │
│  │  ┌───────────────────────────────────────────────────────────┐    │    │
│  │  │  Chain of Responsibility (Validações)                     │    │    │
│  │  │  Required → Size → Language → Format                      │    │    │
│  │  └───────────────────────────────────────────────────────────┘    │    │
│  │                                                                     │    │
│  │  ┌───────────────────────────────────────────────────────────┐    │    │
│  │  │  Pipeline Pattern (6 passos)                              │    │    │
│  │  │  ┌─────────────────────────────────────────────────────┐ │    │    │
│  │  │  │ 1. RemoveDuplicatesStep                             │ │    │    │
│  │  │  │    • Remove textos duplicados                       │ │    │    │
│  │  │  └─────────────────────────────────────────────────────┘ │    │    │
│  │  │  ┌─────────────────────────────────────────────────────┐ │    │    │
│  │  │  │ 2. RemoveSensitiveDataStep (LGPD)                   │ │    │    │
│  │  │  │    • Remove CPF, CNPJ, emails, telefones            │ │    │    │
│  │  │  └─────────────────────────────────────────────────────┘ │    │    │
│  │  │  ┌─────────────────────────────────────────────────────┐ │    │    │
│  │  │  │ 3. CacheConsultStep                                 │ │    │    │
│  │  │  │    • L1 (Caffeine) → L2 (Redis)                     │ │    │    │
│  │  │  └─────────────────────────────────────────────────────┘ │    │    │
│  │  │  ┌─────────────────────────────────────────────────────┐ │    │    │
│  │  │  │ 4. TranslationStep                                  │ │    │    │
│  │  │  │    • Usa Strategy Pattern                           │ │    │    │
│  │  │  │    • Factory seleciona estratégia                   │ │    │    │
│  │  │  └─────────────────────────────────────────────────────┘ │    │    │
│  │  │  ┌─────────────────────────────────────────────────────┐ │    │    │
│  │  │  │ 5. CacheSaveStep                                    │ │    │    │
│  │  │  │    • Salva em L1 e L2                               │ │    │    │
│  │  │  └─────────────────────────────────────────────────────┘ │    │    │
│  │  │  ┌─────────────────────────────────────────────────────┐ │    │    │
│  │  │  │ 6. LogStep                                          │ │    │    │
│  │  │  │    • Métricas e logs                                │ │    │    │
│  │  │  └─────────────────────────────────────────────────────┘ │    │    │
│  │  └───────────────────────────────────────────────────────────┘    │    │
│  │                                                                     │    │
│  │  ┌───────────────────────────────────────────────────────────┐    │    │
│  │  │  Strategy Pattern + Factory                               │    │    │
│  │  │  ┌────────────────────┐  ┌────────────────────────────┐  │    │    │
│  │  │  │ Factory            │  │ Strategies                 │  │    │    │
│  │  │  │ ┌────────────────┐ │  │ • TextTranslation         │  │    │    │
│  │  │  │ │ getStrategy()  │ │  │ • DocumentTranslation     │  │    │    │
│  │  │  │ │  → seleciona   │ │  │ • ImageTranslation        │  │    │    │
│  │  │  │ │    por tipo    │ │  │ • HtmlTranslation         │  │    │    │
│  │  │  │ └────────────────┘ │  └────────────────────────────┘  │    │    │
│  │  │  └────────────────────┘                                   │    │    │
│  │  └───────────────────────────────────────────────────────────┘    │    │
│  │                                                                     │    │
│  │  ┌───────────────────────────────────────────────────────────┐    │    │
│  │  │  Ports (Interfaces)                                       │    │    │
│  │  │  • TranslationPort   • CachePort   • DictionaryPort       │    │    │
│  │  └───────────────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────┬──────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────▼──────────────────────────────────┐    │
│  │                 INFRASTRUCTURE LAYER                               │    │
│  │                                                                     │    │
│  │  ┌───────────────────────────────────────────────────────────┐    │    │
│  │  │  Adapters (Implementações dos Ports)                      │    │    │
│  │  │  ╔═══════════════════════════════════════════════════╗   │    │    │
│  │  │  ║  TranslationAdapter                               ║   │    │    │
│  │  │  ║  • Usa AWS SDK (Translate, Bedrock)              ║   │    │    │
│  │  │  ║  • Circuit Breaker & Retry                        ║   │    │    │
│  │  │  ╚═══════════════════════════════════════════════════╝   │    │    │
│  │  │  ╔═══════════════════════════════════════════════════╗   │    │    │
│  │  │  ║  MultiLevelCacheAdapter                           ║   │    │    │
│  │  │  ║  • L1: Caffeine (local, 10k entries, 1h TTL)     ║   │    │    │
│  │  │  ║  • L2: Redis (distribuído, 24h TTL)              ║   │    │    │
│  │  │  ╚═══════════════════════════════════════════════════╝   │    │    │
│  │  │  ╔═══════════════════════════════════════════════════╗   │    │    │
│  │  │  ║  DynamoDbDictionaryAdapter                        ║   │    │    │
│  │  │  ║  • Repository Pattern                             ║   │    │    │
│  │  │  ║  • Armazena traduções customizadas                ║   │    │    │
│  │  │  ╚═══════════════════════════════════════════════════╝   │    │    │
│  │  └───────────────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────┬────────────────────────────────────────┘
                                      │
        ┌─────────────────────────────┼─────────────────────────────┐
        │                             │                             │
        ▼                             ▼                             ▼
┌───────────────┐           ┌───────────────┐           ┌───────────────┐
│  AWS Services │           │ Cache Layer   │           │   Monitoring  │
├───────────────┤           ├───────────────┤           ├───────────────┤
│ • Translate   │           │ • Caffeine    │           │ • Prometheus  │
│ • Bedrock     │           │ • Redis       │           │ • Actuator    │
│ • DynamoDB    │           └───────────────┘           │ • Micrometer  │
└───────────────┘                                       └───────────────┘
```

## 🔄 Fluxo de Dados Detalhado

```
┌─────────────┐
│   FRONTEND  │
│  (Browser)  │
└──────┬──────┘
       │ 1. HTTP POST /api/v1/translations
       │    { texts: ["Hello"], from: "en", to: "pt" }
       ▼
┌──────────────────────────────────────────────┐
│  TranslationController                       │
│  • Recebe requisição                         │
│  • Valida JSON                               │
└──────┬───────────────────────────────────────┘
       │ 2. Chama TranslationService
       ▼
┌──────────────────────────────────────────────┐
│  TranslationService                          │
│  • Orquestra fluxo                           │
└──────┬───────────────────────────────────────┘
       │ 3. Valida entrada
       ▼
┌──────────────────────────────────────────────┐
│  ValidationChain                             │
│  ├─ RequiredFieldsValidator ✓                │
│  ├─ SizeValidator ✓                          │
│  ├─ LanguageValidator ✓                      │
│  └─ FormatValidator ✓                        │
└──────┬───────────────────────────────────────┘
       │ 4. Cria contexto e executa pipeline
       ▼
┌──────────────────────────────────────────────┐
│  TranslationPipeline                         │
│                                              │
│  Step 1: RemoveDuplicatesStep               │
│  ├─ Input: ["Hello", "World", "Hello"]      │
│  └─ Output: ["Hello", "World"]              │
│                                              │
│  Step 2: RemoveSensitiveDataStep            │
│  ├─ Input: ["My CPF is 123.456.789-00"]     │
│  └─ Output: ["My CPF is [CPF REMOVIDO]"]    │
│                                              │
│  Step 3: CacheConsultStep                   │
│  ├─ Busca em Caffeine (L1) → MISS           │
│  ├─ Busca em Redis (L2) → MISS              │
│  └─ Cache misses: 2                          │
│                                              │
│  Step 4: TranslationStep                    │
│  ├─ Factory seleciona: TextStrategy          │
│  ├─ AWS Translate: "Hello" → "Olá"          │
│  ├─ AWS Translate: "World" → "Mundo"        │
│  └─ Traduções: ["Olá", "Mundo"]             │
│                                              │
│  Step 5: CacheSaveStep                      │
│  ├─ Salva em Caffeine: en:pt:Hello=Olá      │
│  └─ Salva em Redis: en:pt:Hello=Olá         │
│                                              │
│  Step 6: LogStep                            │
│  ├─ Total: 2 textos                          │
│  ├─ Cache hits: 0                            │
│  ├─ Cache misses: 2                          │
│  ├─ Tempo: 234ms                             │
│  └─ Métricas enviadas para Prometheus        │
└──────┬───────────────────────────────────────┘
       │ 5. Retorna TranslationResponse
       ▼
┌──────────────────────────────────────────────┐
│  TranslationResponse                         │
│  {                                           │
│    "results": [                              │
│      {                                       │
│        "originalText": "Hello",              │
│        "translatedText": "Olá",              │
│        "fromCache": false                    │
│      },                                      │
│      {                                       │
│        "originalText": "World",              │
│        "translatedText": "Mundo",            │
│        "fromCache": false                    │
│      }                                       │
│    ],                                        │
│    "metadata": {                             │
│      "totalTexts": 2,                        │
│      "cacheHits": 0,                         │
│      "cacheMisses": 2,                       │
│      "processingTimeMs": 234                 │
│    }                                         │
│  }                                           │
└──────┬───────────────────────────────────────┘
       │ 6. JSON Response
       ▼
┌─────────────┐
│   FRONTEND  │
│  Renderiza  │
└─────────────┘
```

## 🎯 Strategy Pattern - Seleção de Estratégia

```
┌────────────────────────────────────────┐
│  TranslationRequest                    │
│  type = ?                              │
└────────────────┬───────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────┐
│  TranslationStrategyFactory            │
│  getStrategy(type)                     │
└────────────────┬───────────────────────┘
                 │
        ┌────────┼────────┬───────┬──────┐
        │        │        │       │      │
        ▼        ▼        ▼       ▼      ▼
    ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐
    │ TEXT │ │ DOC  │ │ IMG  │ │ HTML │
    └───┬──┘ └───┬──┘ └───┬──┘ └───┬──┘
        │        │        │        │
        ▼        ▼        ▼        ▼
┌────────────────────────────────────────┐
│  AWS        Apache    Amazon    Jsoup  │
│  Translate  Tika +    Bedrock   +      │
│             Translate Claude 3  Transl │
└────────────────────────────────────────┘
```

## 🔒 LGPD - Remoção de Dados Sensíveis

```
┌──────────────────────────────────────────────┐
│  Input Text:                                 │
│  "Meu CPF é 123.456.789-00                  │
│   Email: user@example.com                    │
│   Tel: (11) 99999-9999"                      │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│  RemoveSensitiveDataStep                     │
│  ┌────────────────────────────────────────┐  │
│  │ Regex Patterns:                        │  │
│  │ • CPF_PATTERN                          │  │
│  │ • CNPJ_PATTERN                         │  │
│  │ • EMAIL_PATTERN                        │  │
│  │ • PHONE_PATTERN                        │  │
│  │ • CREDIT_CARD_PATTERN                  │  │
│  └────────────────────────────────────────┘  │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│  Output Text:                                │
│  "Meu CPF é [CPF REMOVIDO]                  │
│   Email: [EMAIL REMOVIDO]                    │
│   Tel: [TELEFONE REMOVIDO]"                  │
│                                              │
│  hadSensitiveData: true                      │
└──────────────────────────────────────────────┘
```

## 📦 Cache Multinível

```
┌──────────────────────────────────────────────┐
│  Request: translate("Hello", en, pt)         │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│  CacheConsultStep                            │
└──────────────┬───────────────────────────────┘
               │
               ▼
      ┌────────┴────────┐
      │                 │
      ▼                 ▼ (if miss)
┌──────────┐    ┌──────────────┐
│ L1 Cache │    │   L2 Cache   │
│ Caffeine │    │    Redis     │
│          │    │              │
│ Local    │    │ Distribuído  │
│ 10k keys │    │ 24h TTL      │
│ 1h TTL   │    │              │
└────┬─────┘    └──────┬───────┘
     │                 │
     │ HIT             │ HIT
     ▼                 ▼
  Return           Promove para L1
  "Olá"            e retorna "Olá"
     │                 │
     └────────┬────────┘
              │
              ▼ (both MISS)
     ┌────────────────┐
     │ TranslationStep│
     │ AWS Translate  │
     └────────┬───────┘
              │
              ▼
         Save to L1+L2
```

## 📊 Métricas e Observabilidade

```
┌────────────────────────────────────────────────────┐
│  Translation Service                               │
│                                                    │
│  ┌──────────────────────────────────────────────┐ │
│  │  @Timed Annotations                          │ │
│  │  • translation.text                          │ │
│  │  • translation.quick                         │ │
│  │  • translation.document                      │ │
│  │  • translation.image                         │ │
│  │  • translation.html                          │ │
│  └──────────────────────────────────────────────┘ │
└────────────────────────┬───────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────┐
│  Micrometer Registry                               │
│  • Counter: requests, errors                       │
│  • Timer: latency                                  │
│  • Gauge: cache_hit_rate                           │
└────────────────────────┬───────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────┐
│  Prometheus (scrape /actuator/prometheus)          │
│                                                    │
│  # HELP translation_text_seconds                   │
│  # TYPE translation_text_seconds summary           │
│  translation_text_seconds_count 1234               │
│  translation_text_seconds_sum 123.456              │
│                                                    │
│  # Cache hit rate                                  │
│  cache_hit_rate{cache="L1"} 0.85                   │
│  cache_hit_rate{cache="L2"} 0.60                   │
└────────────────────────────────────────────────────┘
```

---

**Microsserviço moderno com Design Patterns e Arquitetura Hexagonal! 🚀**
