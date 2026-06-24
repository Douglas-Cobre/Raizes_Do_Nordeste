package com.raizesdonordeste.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Raizes do Nordeste - API Back-End")
                        .version("1.0.0")
                        .description("""
                                API REST para autenticacao, clientes, unidades, cardapio,
                                produtos, estoque, pedidos, pagamentos mock, fidelidade,
                                funcionarios e relatorios gerenciais.

                                Endpoints protegidos aceitam um token JWT no esquema Bearer.
                                """))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
