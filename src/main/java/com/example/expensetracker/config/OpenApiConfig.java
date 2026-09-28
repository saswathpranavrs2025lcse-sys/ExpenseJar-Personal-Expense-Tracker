package com.example.expensetracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI expenseTrackerOpenAPI() {
        final String securitySchemeName = "BearerAuth";
        return new OpenAPI()
            .info(new Info()
                .title("Expense Tracker — Personal Finance SaaS REST API")
                .description("Production-ready Java Spring Boot backend for personal expense tracking with categories, monthly budgets, tiered budget alerts, smart spending insights, CSV export, and JWT security.")
                .version("2.0.0")
                .contact(new Contact()
                    .name("Expense Tracker Team")
                    .email("support@expensetracker.example.com"))
                .license(new License().name("Apache 2.0").url("https://spring.io")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
