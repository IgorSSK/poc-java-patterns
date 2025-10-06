# Docker Guide - MS Translate

## 🐳 Build e Deploy com Docker

### Pré-requisitos
- Docker 20.10+
- Docker Compose 2.0+

## 📦 Construir a Imagem

### Build simples
```bash
docker build -t ms-translate:latest .
```

### Build com cache otimizado
```bash
docker build --build-arg BUILDKIT_INLINE_CACHE=1 -t ms-translate:latest .
```

### Build multi-plataforma (AMD64 + ARM64)
```bash
docker buildx build --platform linux/amd64,linux/arm64 -t ms-translate:latest .
```

## 🚀 Executar o Container

### Executar apenas a aplicação
```bash
docker run -d \
  --name ms-translate \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  ms-translate:latest
```

### Executar com todas as dependências (Redis + LocalStack)
```bash
docker-compose up -d
```

### Ver logs
```bash
# Logs da aplicação
docker-compose logs -f ms-translate

# Logs de todos os serviços
docker-compose logs -f
```

## 🔧 Variáveis de Ambiente

### Aplicação
```bash
# Perfil Spring
SPRING_PROFILES_ACTIVE=prod

# Redis
SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379

# AWS
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key

# JVM
JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
```

## 🏥 Health Checks

### Verificar saúde da aplicação
```bash
curl http://localhost:8080/actuator/health
```

### Verificar métricas
```bash
curl http://localhost:8080/actuator/metrics
```

## 🛠 Comandos Úteis

### Parar todos os serviços
```bash
docker-compose down
```

### Parar e remover volumes (limpa dados)
```bash
docker-compose down -v
```

### Rebuild e restart
```bash
docker-compose up -d --build
```

### Acessar shell do container
```bash
docker exec -it ms-translate sh
```

### Ver uso de recursos
```bash
docker stats ms-translate
```

## 📊 Acessar Serviços

- **Aplicação**: http://localhost:8080
- **API Docs (Swagger)**: http://localhost:8080/swagger-ui.html
- **Actuator**: http://localhost:8080/actuator
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus
- **Redis**: localhost:6379
- **LocalStack**: http://localhost:4566

## 🐛 Troubleshooting

### Container não inicia
```bash
# Ver logs detalhados
docker logs ms-translate

# Ver eventos do container
docker events
```

### Problema de memória
```bash
# Ajustar memória disponível
docker run -m 2g --memory-swap 2g ms-translate:latest
```

### Limpar imagens não utilizadas
```bash
docker image prune -a
```

## 🔐 Produção

### Build para produção (sem LocalStack)
```bash
# Criar docker-compose.prod.yml sem LocalStack
docker-compose -f docker-compose.prod.yml up -d
```

### Push para Registry
```bash
# Fazer login no registry
docker login

# Taguear imagem
docker tag ms-translate:latest your-registry/ms-translate:1.0.0

# Push
docker push your-registry/ms-translate:1.0.0
```

## 📝 Notas

1. **Multi-stage Build**: O Dockerfile usa build em múltiplos estágios para otimizar o tamanho da imagem final
2. **Segurança**: A aplicação roda como usuário não-root (spring:spring)
3. **Health Check**: Container inclui health check automático via Actuator
4. **JVM Tuning**: Configurações otimizadas para ambiente containerizado
5. **Cache**: Dependências Maven são cacheadas para builds mais rápidos

## 🎯 Otimizações Implementadas

- ✅ Multi-stage build (reduz tamanho da imagem)
- ✅ Layer caching do Maven
- ✅ Alpine Linux (imagem base menor)
- ✅ Non-root user (segurança)
- ✅ Health checks
- ✅ JVM container-aware
- ✅ .dockerignore (build mais rápido)
