# Translation Microservice - Arquitetura Hexagonal

Microsserviço Java 21 com Spring Boot para tradução de textos, documentos, imagens e HTML utilizando AWS Translate e Amazon Bedrock, implementado com Arquitetura Hexagonal.

## Padrões de Design
1. Pipeline Pattern - 6 passos sequenciais
2. Strategy Pattern - 4 estratégias de tradução
3. Factory Pattern - Criação de estratégias
4. Adapter Pattern - Integração AWS
5. Repository Pattern - Acesso a dados
6. Chain of Responsibility - Validações

## Features
- Tradução Multi-formato (TEXT, DOCUMENT, IMAGE, HTML)
- Cache Multinível (Caffeine + Redis)
- Dicionário DynamoDB
- Conformidade LGPD
- Circuit Breaker
- Métricas

## Tecnologias
Java 21, Spring Boot 3.2.0, AWS SDK, Caffeine, Redis, Apache Tika, Jsoup, Resilience4j

## Autor
Igor Souza - @IgorSSK