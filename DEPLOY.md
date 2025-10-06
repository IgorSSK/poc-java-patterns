# Guia de Deploy - Translation Microservice

## 🚀 Opções de Deploy

### 1. Deploy Local (Desenvolvimento)

#### Pré-requisitos
```bash
# Java 21
java -version

# Maven
mvn -version

# Docker (para Redis)
docker --version

# AWS CLI (configurado)
aws configure
```

#### Passos

1. **Iniciar Redis**
```bash
docker run -d --name redis -p 6379:6379 redis:latest
```

2. **Configurar AWS Credentials**
```bash
export AWS_ACCESS_KEY_ID=your_key
export AWS_SECRET_ACCESS_KEY=your_secret
export AWS_REGION=us-east-1
```

3. **Criar tabela DynamoDB (opcional)**
```bash
aws dynamodb create-table \
  --table-name translation-dictionary \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1
```

4. **Compilar e executar**
```bash
./mvnw clean package
./mvnw spring-boot:run
```

5. **Verificar**
```bash
curl http://localhost:8080/actuator/health
```

---

### 2. Deploy com Docker

#### Criar Dockerfile
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/translation-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Criar docker-compose.yml
```yaml
version: '3.8'

services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 5

  translation-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
      - AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
      - AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
      - AWS_REGION=us-east-1
    depends_on:
      redis:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  redis-data:
```

#### Executar
```bash
# Build
./mvnw clean package -DskipTests
docker-compose build

# Run
docker-compose up -d

# Logs
docker-compose logs -f translation-service

# Stop
docker-compose down
```

---

### 3. Deploy Kubernetes (AWS EKS)

#### ConfigMap
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: translation-config
data:
  application.yml: |
    server:
      port: 8080
    spring:
      redis:
        host: redis-service
        port: 6379
    aws:
      region: us-east-1
```

#### Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: translation-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: translation-service
  template:
    metadata:
      labels:
        app: translation-service
    spec:
      containers:
      - name: translation-service
        image: your-registry/translation-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: AWS_REGION
          value: "us-east-1"
        - name: SPRING_REDIS_HOST
          value: "redis-service"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
        volumeMounts:
        - name: config
          mountPath: /app/config
      volumes:
      - name: config
        configMap:
          name: translation-config
---
apiVersion: v1
kind: Service
metadata:
  name: translation-service
spec:
  type: LoadBalancer
  selector:
    app: translation-service
  ports:
  - port: 80
    targetPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: redis-service
spec:
  selector:
    app: redis
  ports:
  - port: 6379
    targetPort: 6379
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
```

#### Deploy
```bash
# Criar namespace
kubectl create namespace translation

# Apply
kubectl apply -f k8s/ -n translation

# Verificar
kubectl get pods -n translation
kubectl get svc -n translation

# Logs
kubectl logs -f deployment/translation-service -n translation

# Scale
kubectl scale deployment translation-service --replicas=5 -n translation
```

---

### 4. Deploy AWS ECS Fargate

#### task-definition.json
```json
{
  "family": "translation-service",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::ACCOUNT:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::ACCOUNT:role/translationServiceRole",
  "containerDefinitions": [
    {
      "name": "translation-service",
      "image": "ACCOUNT.dkr.ecr.us-east-1.amazonaws.com/translation-service:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "AWS_REGION",
          "value": "us-east-1"
        },
        {
          "name": "SPRING_REDIS_HOST",
          "value": "your-elasticache-endpoint"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/translation-service",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:8080/actuator/health || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3
      }
    }
  ]
}
```

#### Deploy
```bash
# Build e push para ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ACCOUNT.dkr.ecr.us-east-1.amazonaws.com
docker build -t translation-service .
docker tag translation-service:latest ACCOUNT.dkr.ecr.us-east-1.amazonaws.com/translation-service:latest
docker push ACCOUNT.dkr.ecr.us-east-1.amazonaws.com/translation-service:latest

# Registrar task definition
aws ecs register-task-definition --cli-input-json file://task-definition.json

# Criar serviço
aws ecs create-service \
  --cluster translation-cluster \
  --service-name translation-service \
  --task-definition translation-service \
  --desired-count 3 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx,subnet-yyy],securityGroups=[sg-xxx],assignPublicIp=ENABLED}" \
  --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:...,containerName=translation-service,containerPort=8080"
```

---

### 5. Deploy Serverless (AWS Lambda)

#### Adicionar dependência ao pom.xml
```xml
<dependency>
    <groupId>com.amazonaws.serverless</groupId>
    <artifactId>aws-serverless-java-container-springboot3</artifactId>
    <version>2.0.0</version>
</dependency>
```

#### Lambda Handler
```java
package com.translation;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.InputStream;
import java.io.OutputStream;

public class LambdaHandler implements RequestStreamHandler {
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(TranslationApplication.class);
        } catch (ContainerInitializationException e) {
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) {
        handler.proxyStream(input, output, context);
    }
}
```

#### SAM template.yaml
```yaml
AWSTemplateFormatVersion: '2010-09-09'
Transform: AWS::Serverless-2016-10-31

Resources:
  TranslationFunction:
    Type: AWS::Serverless::Function
    Properties:
      Handler: com.translation.LambdaHandler::handleRequest
      Runtime: java21
      CodeUri: target/translation-service.jar
      MemorySize: 1024
      Timeout: 30
      Environment:
        Variables:
          AWS_REGION: us-east-1
          SPRING_REDIS_HOST: !GetAtt RedisCluster.RedisEndpoint.Address
      Events:
        ApiEvent:
          Type: Api
          Properties:
            Path: /{proxy+}
            Method: ANY
```

---

## 🔧 Variáveis de Ambiente

```bash
# AWS
export AWS_REGION=us-east-1
export AWS_ACCESS_KEY_ID=your_key
export AWS_SECRET_ACCESS_KEY=your_secret

# Redis
export SPRING_REDIS_HOST=localhost
export SPRING_REDIS_PORT=6379

# DynamoDB
export AWS_DYNAMODB_TABLE_NAME=translation-dictionary

# Cache
export TRANSLATION_CACHE_TTL=86400

# Logging
export LOGGING_LEVEL_COM_TRANSLATION=DEBUG

# Server
export SERVER_PORT=8080
```

---

## 📊 Monitoramento

### Prometheus (scrape config)
```yaml
scrape_configs:
  - job_name: 'translation-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['translation-service:8080']
```

### Grafana Dashboard
```json
{
  "dashboard": {
    "title": "Translation Service",
    "panels": [
      {
        "title": "Request Rate",
        "targets": [
          {
            "expr": "rate(translation_text_seconds_count[5m])"
          }
        ]
      },
      {
        "title": "Latency P95",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, translation_text_seconds_bucket)"
          }
        ]
      },
      {
        "title": "Cache Hit Rate",
        "targets": [
          {
            "expr": "cache_hit_rate"
          }
        ]
      }
    ]
  }
}
```

---

## 🔒 Segurança

### IAM Policy (AWS)
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "translate:TranslateText"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "bedrock:InvokeModel"
      ],
      "Resource": "arn:aws:bedrock:*:*:model/anthropic.claude-*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:GetItem",
        "dynamodb:PutItem",
        "dynamodb:Query"
      ],
      "Resource": "arn:aws:dynamodb:*:*:table/translation-dictionary"
    }
  ]
}
```

---

## 📈 Scaling

### Horizontal Pod Autoscaler (K8s)
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: translation-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: translation-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

---

## ✅ Checklist de Deploy

- [ ] Redis configurado e acessível
- [ ] AWS credentials configuradas
- [ ] DynamoDB table criada
- [ ] Variáveis de ambiente configuradas
- [ ] Health check funcionando (`/actuator/health`)
- [ ] Métricas expostas (`/actuator/prometheus`)
- [ ] Logs estruturados configurados
- [ ] Circuit breaker testado
- [ ] Cache multinível funcionando
- [ ] Testes de carga realizados
- [ ] Backup strategy definida
- [ ] Monitoramento configurado (Prometheus + Grafana)
- [ ] Alertas configurados
- [ ] Documentação atualizada

---

**Deploy bem-sucedido! 🚀**
