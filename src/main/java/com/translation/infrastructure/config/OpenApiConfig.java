package com.translation.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Swagger/OpenAPI
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI translationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Translation Microservice API")
                        .description("Microsserviço de tradução multi-formato com Arquitetura Hexagonal\n\n" +
                                "## Padrões Implementados:\n" +
                                "- **Pipeline Pattern**: 6 passos sequenciais\n" +
                                "- **Strategy Pattern**: 4 estratégias de tradução\n" +
                                "- **Factory Pattern**: Seleção de estratégias\n" +
                                "- **Adapter Pattern**: Integração AWS\n" +
                                "- **Repository Pattern**: DynamoDB\n" +
                                "- **Chain of Responsibility**: Validações\n\n" +
                                "## Features:\n" +
                                "- Cache multinível (Caffeine + Redis)\n" +
                                "- Remoção de dados sensíveis (LGPD)\n" +
                                "- Circuit Breaker e Retry\n" +
                                "- Métricas Prometheus")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Igor Souza")
                                .url("https://github.com/IgorSSK"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
