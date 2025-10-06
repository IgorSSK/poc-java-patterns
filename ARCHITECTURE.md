# Documentação da Arquitetura - Translation Microservice

## 📐 Arquitetura Hexagonal (Ports & Adapters)

### O que é Arquitetura Hexagonal?

A Arquitetura Hexagonal, também conhecida como Ports & Adapters, foi proposta por Alistair Cockburn. O objetivo principal é **isolar o domínio da aplicação das dependências externas** (frameworks, bancos de dados, APIs externas, etc.).

### Benefícios

✅ **Testabilidade**: Domínio pode ser testado sem dependências externas  
✅ **Flexibilidade**: Fácil trocar implementações (ex: trocar Redis por Memcached)  
✅ **Independência de Framework**: Domínio não depende do Spring Boot  
✅ **Manutenibilidade**: Mudanças em infraestrutura não afetam regras de negócio  
✅ **Clean Code**: Separação clara de responsabilidades  

---

## 🏗️ Camadas da Aplicação

### 1. **Domain Layer** (Núcleo)
**Localização**: `com.translation.domain`

**Responsabilidade**: Contém toda a lógica de negócio e regras do domínio. É **independente** de frameworks e infraestrutura.

**Componentes**:
- `model/` - Entidades, VOs, DTOs do domínio
- `pipeline/` - Pipeline Pattern com 6 passos
- `strategy/` - Strategy Pattern com 4 estratégias
- `factory/` - Factory para criar estratégias
- `validator/` - Chain of Responsibility para validações
- `port/` - **Interfaces** que definem contratos (Ports)
- `exception/` - Exceções do domínio

**Regra de Ouro**: O domínio **não conhece** a infraestrutura. Ele apenas define interfaces (Ports).

---

### 2. **Application Layer** (Casos de Uso)
**Localização**: `com.translation.application`

**Responsabilidade**: Orquestra o fluxo de execução, coordena o domínio e adapters.

**Componentes**:
- `service/` - Services que orquestram casos de uso
- `controller/` - REST Controllers (Adapter de entrada)

**Exemplo**: `TranslationService` recebe uma requisição, valida, executa o pipeline e retorna resposta.

---

### 3. **Infrastructure Layer** (Detalhes Técnicos)
**Localização**: `com.translation.infrastructure`

**Responsabilidade**: Implementa os Ports definidos no domínio. Contém toda a comunicação com o mundo externo.

**Componentes**:
- `adapter/` - **Implementações** dos Ports (Adapters)
- `config/` - Configurações de beans, AWS, cache, etc.

**Adapters implementados**:
- `TranslationAdapter` → implementa `TranslationPort`
- `MultiLevelCacheAdapter` → implementa `CachePort`
- `DynamoDbDictionaryAdapter` → implementa `DictionaryPort`

---

## 🔌 Ports & Adapters

### Ports (Interfaces no Domínio)

**Port** = Interface que define o que o domínio **precisa** ou **fornece**.

Existem dois tipos:

#### 1. **Primary Ports** (Driving/Inbound)
Interfaces que o domínio **fornece** para o mundo externo.

**Exemplo**: `TranslationService` (orquestra o caso de uso)

#### 2. **Secondary Ports** (Driven/Outbound)
Interfaces que o domínio **precisa** do mundo externo.

**Exemplos**:
```java
// Port para tradução (precisa de AWS)
public interface TranslationPort {
    List<String> translate(...);
}

// Port para cache (precisa de Redis/Caffeine)
public interface CachePort {
    String get(String key);
    void put(String key, String value);
}

// Port para persistência (precisa de DynamoDB)
public interface DictionaryPort {
    Optional<String> findTranslation(...);
}
```

---

### Adapters (Implementações na Infraestrutura)

**Adapter** = Implementação concreta de um Port que se conecta ao mundo externo.

#### 1. **Primary Adapters** (Entrada)
Recebem requisições externas e chamam o domínio.

**Exemplo**: `TranslationController` (REST API)

#### 2. **Secondary Adapters** (Saída)
Implementam Ports do domínio para acessar recursos externos.

**Exemplos**:
```java
// Implementa TranslationPort usando AWS SDK
@Component
public class TranslationAdapter implements TranslationPort {
    private final TranslationStrategyFactory factory;
    // ...
}

// Implementa CachePort usando Caffeine + Redis
@Component
public class MultiLevelCacheAdapter implements CachePort {
    private final CacheManager cacheManager;
    private final StringRedisTemplate redisTemplate;
    // ...
}
```

---

## 🎨 Padrões de Design Detalhados

### 1. Pipeline Pattern 🔄

**Problema**: Como processar dados através de múltiplas etapas sequenciais?

**Solução**: Pipeline de 6 passos que transformam um `TranslationContext`.

```
Input → Step1 → Step2 → Step3 → Step4 → Step5 → Step6 → Output
```

**Fluxo**:
1. `RemoveDuplicatesStep` - Remove textos duplicados
2. `RemoveSensitiveDataStep` - Sanitiza dados sensíveis (LGPD)
3. `CacheConsultStep` - Consulta cache L1/L2
4. `TranslationStep` - Traduz usando Strategy Pattern
5. `CacheSaveStep` - Persiste traduções novas
6. `LogStep` - Coleta métricas

**Benefícios**:
- ✅ Cada passo tem responsabilidade única (SRP)
- ✅ Fácil adicionar/remover/reordenar passos
- ✅ Testável isoladamente
- ✅ Trace completo do processamento

---

### 2. Strategy Pattern 🎯

**Problema**: Como escolher algoritmo de tradução dinamicamente baseado no tipo de conteúdo?

**Solução**: Interface `TranslationStrategy` com 4 implementações especializadas.

```
TranslationStrategy (interface)
    ├── TextTranslationStrategy (AWS Translate)
    ├── DocumentTranslationStrategy (Tika + AWS)
    ├── ImageTranslationStrategy (Bedrock Claude 3)
    └── HtmlTranslationStrategy (Jsoup + AWS)
```

**Decisão de estratégia**:
- `TEXT` → AWS Translate direto
- `DOCUMENT` → Extrai com Tika → Traduz com chunking
- `IMAGE` → OCR + tradução com Bedrock
- `HTML` → Preserva estrutura com Jsoup → Traduz texto

**Benefícios**:
- ✅ Algoritmos intercambiáveis
- ✅ Fácil adicionar novos tipos
- ✅ Cada estratégia é independente
- ✅ Open/Closed Principle (OCP)

---

### 3. Factory Pattern 🏭

**Problema**: Como criar a estratégia correta sem acoplar código cliente?

**Solução**: `TranslationStrategyFactory` seleciona estratégia baseada em `TranslationType`.

```java
@Component
public class TranslationStrategyFactory {
    private final Map<TranslationType, TranslationStrategy> strategies;
    
    public TranslationStrategy getStrategy(TranslationType type) {
        return strategies.get(type);
    }
}
```

**Benefícios**:
- ✅ Encapsula lógica de criação
- ✅ Desacopla cliente das implementações
- ✅ Fácil adicionar novas estratégias

---

### 4. Adapter Pattern 🔌

**Problema**: Como integrar com APIs externas sem acoplar domínio?

**Solução**: Adapters implementam Ports usando SDKs externos.

**Exemplo - AWS Translate**:
```java
// Port (domínio)
public interface TranslationPort {
    List<String> translate(...);
}

// Adapter (infraestrutura)
@Component
public class TranslationAdapter implements TranslationPort {
    private final TranslateClient awsClient; // AWS SDK
    
    public List<String> translate(...) {
        // Adapta chamada AWS SDK para o domínio
    }
}
```

**Benefícios**:
- ✅ Domínio isolado de dependências externas
- ✅ Fácil trocar AWS por Google Translate
- ✅ Mockável para testes

---

### 5. Repository Pattern 📦

**Problema**: Como abstrair acesso a dados?

**Solução**: `DictionaryPort` + `DynamoDbDictionaryAdapter`.

```java
// Port (domínio)
public interface DictionaryPort {
    Optional<String> findTranslation(...);
    void saveTranslation(...);
}

// Adapter (infraestrutura)
@Component
public class DynamoDbDictionaryAdapter implements DictionaryPort {
    private final DynamoDbEnhancedClient client;
    // Implementa usando DynamoDB
}
```

**Benefícios**:
- ✅ Abstrai fonte de dados
- ✅ Fácil trocar DynamoDB por PostgreSQL
- ✅ Testável com mock

---

### 6. Chain of Responsibility ⛓️

**Problema**: Como validar requisição através de múltiplas regras?

**Solução**: Cadeia de 4 validadores encadeados.

```
Request → RequiredFields → Size → Language → Format → ✅
            ❌ throw        ❌ throw  ❌ throw   ❌ throw
```

**Validadores**:
1. `RequiredFieldsValidator` - Campos obrigatórios
2. `SizeValidator` - Limites de tamanho (1000 textos, 10MB)
3. `LanguageValidator` - Idiomas suportados
4. `FormatValidator` - Tipo vs conteúdo

**Benefícios**:
- ✅ Cada validador tem responsabilidade única
- ✅ Fácil adicionar/remover validadores
- ✅ Falha rápida (fail-fast)

---

## 🔄 Fluxo Completo de uma Requisição

```
1. Frontend envia POST /api/v1/translations
   ↓
2. TranslationController (Primary Adapter)
   ↓
3. TranslationService (Application)
   ↓
4. ValidationChain executa validadores (Chain of Responsibility)
   ↓
5. TranslationPipeline executa 6 passos (Pipeline Pattern)
   │
   ├─ Step 1: Remove duplicidades
   ├─ Step 2: Remove dados sensíveis (CPF, email, etc)
   ├─ Step 3: Consulta cache (MultiLevelCacheAdapter - L1/L2)
   ├─ Step 4: Traduz (TranslationAdapter usa Strategy Pattern)
   │           │
   │           └─ TranslationStrategyFactory seleciona estratégia
   │                   │
   │                   ├─ TextTranslationStrategy (AWS Translate)
   │                   ├─ DocumentTranslationStrategy (Tika + AWS)
   │                   ├─ ImageTranslationStrategy (Bedrock)
   │                   └─ HtmlTranslationStrategy (Jsoup + AWS)
   │
   ├─ Step 5: Salva no cache (L1 + L2)
   └─ Step 6: Log e métricas
   ↓
6. TranslationResponse retornada
   ↓
7. Frontend recebe JSON
```

---

## 🧪 Testabilidade

### Por que essa arquitetura é altamente testável?

1. **Domínio Isolado**: Pode ser testado sem Spring, AWS, Redis, etc.
2. **Mocks Fáceis**: Ports são interfaces → fácil mockar
3. **Passos Independentes**: Cada pipeline step pode ser testado isoladamente
4. **Estratégias Isoladas**: Cada strategy pode ser testada separadamente

### Exemplo de Teste

```java
@Test
void shouldTranslateTexts() {
    // Mock dos Ports
    TranslationPort mockPort = mock(TranslationPort.class);
    CachePort mockCache = mock(CachePort.class);
    
    // Testa domínio puro, sem infraestrutura real
    TranslationService service = new TranslationService(
        validationChain, 
        pipeline
    );
    
    TranslationResponse response = service.translate(request);
    
    assertThat(response).isNotNull();
}
```

---

## 🚀 Escalabilidade

### Como escalar este microsserviço?

1. **Cache Multinível**: L1 (Caffeine local) + L2 (Redis distribuído)
2. **Stateless**: Não mantém estado, pode escalar horizontalmente
3. **Circuit Breaker**: Protege AWS de sobrecarga
4. **Retry**: 3 tentativas com backoff
5. **Métricas**: Prometheus monitora performance

---

## 📊 Observabilidade

### Métricas Coletadas

- **Latência**: Tempo de cada tipo de tradução
- **Cache Hit Rate**: Taxa de acerto do cache
- **Erros**: Falhas de tradução, validação
- **Circuit Breaker**: Estado do circuit breaker

### Logs Estruturados

Cada passo do pipeline loga:
- Entrada/saída
- Tempo de execução
- Erros

---

## 🔒 Segurança e LGPD

### RemoveSensitiveDataStep

Remove automaticamente:
- **CPF**: `123.456.789-00` → `[CPF REMOVIDO]`
- **CNPJ**: `12.345.678/0001-90` → `[CNPJ REMOVIDO]`
- **Email**: `user@example.com` → `[EMAIL REMOVIDO]`
- **Telefone**: `(11) 99999-9999` → `[TELEFONE REMOVIDO]`
- **Cartão**: `1234-5678-9012-3456` → `[CARTÃO REMOVIDO]`

---

## 📚 Referências

- **Hexagonal Architecture**: Alistair Cockburn
- **Clean Architecture**: Robert C. Martin (Uncle Bob)
- **Domain-Driven Design**: Eric Evans
- **Design Patterns**: Gang of Four (GoF)

---

**Arquitetura pensada para ser manutenível, testável e escalável! 🚀**
