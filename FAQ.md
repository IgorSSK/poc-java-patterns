# ❓ FAQ - Perguntas Frequentes

## 📋 Geral

### P: Por que Arquitetura Hexagonal?
**R**: A Arquitetura Hexagonal (Ports & Adapters) isola o domínio da aplicação das dependências externas. Isso significa:
- ✅ Domínio testável sem AWS, Redis, banco de dados
- ✅ Fácil trocar implementações (ex: Redis → Memcached)
- ✅ Independente de frameworks
- ✅ Código mais limpo e manutenível

### P: Quantos Design Patterns foram implementados?
**R**: **6 patterns principais**:
1. Pipeline Pattern
2. Strategy Pattern
3. Factory Pattern
4. Adapter Pattern
5. Repository Pattern
6. Chain of Responsibility

### P: Qual é o fluxo completo de uma requisição?
**R**: 
```
Frontend → Controller → Service → ValidationChain → Pipeline (6 steps) → Response
```
Veja diagramas detalhados em [DIAGRAMS.md](DIAGRAMS.md).

---

## 🎨 Design Patterns

### P: Por que usar Pipeline Pattern?
**R**: Permite processar dados através de múltiplos passos sequenciais com responsabilidade única. Cada passo pode ser:
- Testado isoladamente
- Adicionado/removido facilmente
- Reordenado conforme necessário
- Trackeado para debugging

### P: Qual a diferença entre Strategy e Factory?
**R**: 
- **Strategy Pattern**: Define *como* traduzir (algoritmos diferentes)
- **Factory Pattern**: Define *qual* estratégia usar (seleção)

### P: Por que Chain of Responsibility para validação?
**R**: Permite encadear múltiplos validadores onde cada um tem responsabilidade única. Vantagens:
- Fail-fast (para no primeiro erro)
- Fácil adicionar/remover validadores
- Código mais limpo (SRP - Single Responsibility Principle)

---

## 🔧 Implementação

### P: Como adicionar um novo tipo de tradução?
**R**: 
1. Adicione enum em `TranslationType`
2. Crie nova estratégia implementando `TranslationStrategy`
3. Anote com `@Component`
4. Factory registra automaticamente!

```java
@Component
public class VideoTranslationStrategy implements TranslationStrategy {
    @Override
    public TranslationType getType() {
        return TranslationType.VIDEO;
    }
    // ...
}
```

### P: Como adicionar um novo passo no pipeline?
**R**:
1. Crie classe implementando `PipelineStep`
2. Anote com `@Component`
3. Adicione no construtor de `TranslationPipeline` na ordem desejada

```java
@Component
public class MyCustomStep implements PipelineStep {
    @Override
    public TranslationContext execute(TranslationContext context) {
        // sua lógica
        return context;
    }
}
```

### P: Como adicionar um novo validador?
**R**:
1. Estenda `AbstractValidationHandler`
2. Implemente `doValidate()`
3. Adicione na cadeia em `ValidationChain`

```java
@Component
public class CustomValidator extends AbstractValidationHandler {
    @Override
    protected void doValidate(TranslationRequest request) {
        // validação customizada
    }
}
```

---

## 🚀 Performance

### P: Como funciona o cache multinível?
**R**: 
- **L1 (Caffeine)**: Cache local, muito rápido, 10k entries, 1h TTL
- **L2 (Redis)**: Cache distribuído, compartilhado, 24h TTL

**Fluxo**:
1. Busca em L1 → HIT? Retorna
2. Busca em L2 → HIT? Promove para L1 e retorna
3. MISS em ambos → Traduz → Salva em L1+L2

### P: Qual a taxa de hit do cache esperada?
**R**: Depende do uso, mas geralmente:
- **L1**: 70-85% (requisições recentes)
- **L2**: 50-70% (requisições menos frequentes)
- **Overall**: 80-90% com uso normal

### P: Como melhorar a performance?
**R**:
1. ✅ Cache warming (pré-carregar traduções comuns)
2. ✅ Batch translations (traduzir múltiplos textos juntos)
3. ✅ Aumentar TTL do cache
4. ✅ Usar Redis cluster
5. ✅ Async processing para requests grandes
6. ✅ CDN para assets estáticos

---

## 🔒 Segurança e LGPD

### P: Quais dados sensíveis são removidos?
**R**: O `RemoveSensitiveDataStep` remove:
- CPF (123.456.789-00)
- CNPJ (12.345.678/0001-90)
- Emails (user@example.com)
- Telefones ((11) 99999-9999)
- Cartões de crédito (1234-5678-9012-3456)

### P: Os dados sensíveis são armazenados antes de serem removidos?
**R**: **Não**. A remoção ocorre no Step 2 do pipeline, **antes** de qualquer tradução ou cache. Fluxo:
```
Input → RemoveDuplicates → RemoveSensitiveData → Cache → Translate
```

### P: Como adicionar novo tipo de dado sensível?
**R**: Adicione Pattern em `RemoveSensitiveDataStep`:
```java
private static final Pattern RG_PATTERN = Pattern.compile("\\d{2}\\.\\d{3}\\.\\d{3}-\\d{1}");
// ...
if (RG_PATTERN.matcher(sanitized).find()) {
    sanitized = RG_PATTERN.matcher(sanitized).replaceAll("[RG REMOVIDO]");
    hasSensitive = true;
}
```

---

## 🌐 AWS Services

### P: Preciso de conta AWS para testar?
**R**: **Sim**, para funcionalidade completa. Mas você pode:
- Mockar os adapters para desenvolvimento local
- Usar LocalStack para emular AWS
- Implementar fallback strategies sem AWS

### P: Quais serviços AWS são usados?
**R**:
- **AWS Translate**: Tradução de texto e documentos
- **Amazon Bedrock**: Claude 3 para OCR em imagens
- **DynamoDB**: Dicionário de traduções customizadas

### P: Quanto custa usar AWS Translate?
**R**: (Preços aproximados, verificar site AWS)
- AWS Translate: $15 por milhão de caracteres
- Bedrock Claude 3: $3-15 por milhão de tokens
- DynamoDB: $0.25 por milhão de leituras
- Com cache 80%, custo reduz ~5x

### P: Como trocar AWS Translate por Google Translate?
**R**: Graças à Arquitetura Hexagonal:
1. Crie `GoogleTranslateAdapter implements TranslationPort`
2. Use Google Cloud Translation API
3. Configure no Spring (não muda domínio!)

```java
@Component
@Primary // Use esta implementação
public class GoogleTranslateAdapter implements TranslationPort {
    private final TranslationServiceClient client;
    // ...
}
```

---

## 📊 Observabilidade

### P: Quais métricas são coletadas?
**R**:
- Latência de tradução por tipo
- Taxa de hit/miss do cache (L1 e L2)
- Número de requisições
- Taxa de erro
- Estado do Circuit Breaker
- Duplicatas removidas
- Dados sensíveis encontrados

### P: Como visualizar métricas?
**R**:
1. **Prometheus**: scrape `/actuator/prometheus`
2. **Grafana**: importe dashboard ou crie custom
3. **Logs**: veja em `logs/` ou stdout

### P: Como configurar alertas?
**R**: Configure no Prometheus:
```yaml
groups:
  - name: translation_alerts
    rules:
      - alert: HighErrorRate
        expr: rate(translation_errors_total[5m]) > 0.05
        annotations:
          summary: "Alta taxa de erros em tradução"
```

---

## 🧪 Testes

### P: Como testar sem AWS real?
**R**: Mock os Ports:
```java
@Test
void testTranslation() {
    TranslationPort mockPort = mock(TranslationPort.class);
    when(mockPort.translate(...)).thenReturn(List.of("Olá"));
    
    // Teste domínio puro
    TranslationService service = new TranslationService(...);
    // ...
}
```

### P: Como executar testes?
**R**:
```bash
# Todos os testes
./mvnw test

# Específico
./mvnw test -Dtest=TranslationServiceTest

# Com coverage
./mvnw test jacoco:report
```

### P: Como testar integração com AWS?
**R**: Use testes de integração com `@SpringBootTest`:
```java
@SpringBootTest
@ActiveProfiles("test")
class TranslationIntegrationTest {
    @Autowired
    private TranslationService service;
    
    @Test
    void testRealAWS() {
        // Testa com AWS real (cuidado com custos!)
    }
}
```

---

## 🚢 Deploy

### P: Qual a melhor opção de deploy?
**R**: Depende do seu caso:
- **Kubernetes (EKS)**: Melhor para produção escalável
- **Docker Compose**: Ótimo para dev/test
- **ECS Fargate**: Serverless com AWS
- **Lambda**: Serverless total (cold start issue)

### P: Preciso de Redis em produção?
**R**: **Recomendado** mas não obrigatório:
- **Com Redis**: Cache distribuído, melhor hit rate
- **Sem Redis**: Apenas cache local (Caffeine), cada instância tem cache próprio

Para remover Redis:
1. Mantenha apenas Caffeine em `CacheConfig`
2. Adapte `MultiLevelCacheAdapter` para usar só L1

### P: Como escalar horizontalmente?
**R**: O serviço é stateless, então:
```bash
# Kubernetes
kubectl scale deployment translation-service --replicas=10

# Docker
docker-compose up -d --scale translation-service=5

# ECS
aws ecs update-service --desired-count 10
```

### P: Qual tamanho de instância usar?
**R**: Recomendações:
- **Dev/Test**: 512MB RAM, 0.5 vCPU
- **Produção**: 1-2GB RAM, 1-2 vCPU
- **High Load**: 4GB+ RAM, 2-4 vCPU

---

## 🔧 Troubleshooting

### P: Circuit Breaker está aberto, o que fazer?
**R**:
1. Verifique logs de erro
2. Confirme conectividade com AWS
3. Verifique credenciais AWS
4. Aguarde `waitDurationInOpenState` (10s)
5. Circuit breaker testa automaticamente (half-open)

### P: Cache não está funcionando
**R**: Checklist:
- [ ] Redis está rodando? `docker ps`
- [ ] Conexão Redis OK? `redis-cli ping`
- [ ] Caffeine configurado? Veja `CacheConfig`
- [ ] Chaves corretas? Veja logs debug

### P: Erro "UnsupportedLanguageException"
**R**: Idioma não suportado. Idiomas disponíveis:
```
pt, en, es, fr, de, it, ja, ko, zh, ar, ru
```

Para adicionar idioma, adicione em:
1. `TranslationAdapter.SUPPORTED_LANGUAGES`
2. Configure AWS Translate para suportar

### P: Performance está lenta
**R**: Investigar:
1. **Cache hit rate**: Deve ser >70%
2. **AWS latency**: Verifique região
3. **Tamanho dos textos**: Chunking funciona?
4. **Circuit breaker**: Está causando delays?
5. **Logs**: Algum step está lento?

---

## 📚 Documentação

### P: Onde encontrar documentação da API?
**R**:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Exemplos**: [api-examples.http](api-examples.http)

### P: Como contribuir com o projeto?
**R**:
1. Fork o repositório
2. Crie branch: `git checkout -b feature/nova-feature`
3. Commit: `git commit -m 'Add nova feature'`
4. Push: `git push origin feature/nova-feature`
5. Abra Pull Request

### P: Onde reportar bugs?
**R**: Abra issue no GitHub com:
- Descrição do bug
- Steps to reproduce
- Logs relevantes
- Versão do Java, Spring Boot
- Environment (local, Docker, K8s)

---

## 💡 Boas Práticas

### P: Como organizar código seguindo Hexagonal?
**R**: Regras:
1. **Domain** não conhece Infrastructure
2. **Domain** define Ports (interfaces)
3. **Infrastructure** implementa Ports (Adapters)
4. **Application** orquestra Domain
5. Dependências apontam para dentro (Domain)

### P: Como manter código limpo?
**R**:
- ✅ SOLID principles
- ✅ SRP: cada classe uma responsabilidade
- ✅ DRY: não repita código
- ✅ Meaningful names
- ✅ Small functions
- ✅ Tests first
- ✅ Code review

### P: Quando criar novo Pattern?
**R**: Crie pattern quando:
- ✅ Problema se repete (DRY)
- ✅ Complexidade cresce
- ✅ Múltiplas variações (Strategy)
- ✅ Processamento multi-step (Pipeline)
- ✅ Abstração necessária (Adapter)

**Não crie** pattern para:
- ❌ Problema único
- ❌ Over-engineering
- ❌ Premature optimization

---

## 🎓 Aprendizado

### P: É obrigatório usar todos esses patterns?
**R**: **Não**! Cada pattern resolve um problema específico:
- Projeto pequeno? Use só o necessário
- Projeto grande? Patterns evitam complexidade

### P: Como aprender mais sobre Hexagonal Architecture?
**R**: Recursos:
- **Livros**: "Clean Architecture" (Uncle Bob)
- **Artigos**: Alistair Cockburn (criador)
- **Vídeos**: YouTube "Hexagonal Architecture"
- **Código**: Este projeto é exemplo completo!

### P: Quais outros patterns posso adicionar?
**R**: Patterns úteis:
- **Observer**: Para eventos assíncronos
- **Command**: Para undo/redo
- **Decorator**: Para adicionar funcionalidades
- **Facade**: Para simplificar interface complexa
- **Singleton**: Para recursos únicos (cuidado!)

---

## 🔮 Futuro

### P: Roadmap do projeto?
**R**: Próximas features:
- [ ] Autenticação OAuth2/JWT
- [ ] Rate limiting
- [ ] WebSocket para streaming
- [ ] GraphQL API
- [ ] Machine Learning para qualidade
- [ ] Suporte a mais formatos (Excel, PPT)
- [ ] Multi-tenancy
- [ ] A/B testing

### P: Como posso contribuir?
**R**: Várias formas:
1. Reportar bugs
2. Sugerir features
3. Melhorar documentação
4. Adicionar testes
5. Otimizar performance
6. Criar exemplos

---

## 📞 Contato

### P: Onde tirar dúvidas?
**R**:
- **Issues**: GitHub Issues
- **Email**: (se disponível)
- **Documentação**: Veja [README.md](README.md)

### P: Como dar feedback?
**R**: Adoramos feedback! Use:
- GitHub Issues (bugs/features)
- GitHub Discussions (dúvidas gerais)
- Pull Requests (contribuições)
- Star ⭐ no repo (se gostou!)

---

**Tem mais perguntas? Abra uma issue no GitHub! 🚀**
