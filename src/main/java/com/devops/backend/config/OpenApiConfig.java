package com.devops.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "DevOps Backend API",
                version = "1.0.0",
                description = "API REST para la gestión de usuarios, autenticación, accesos, roles, funcionalidades y procesos de seguridad del sistema.",
                contact = @Contact(
                        name = "Backend Team",
                        email = "backend@devops.com",
                        url = "https://devops-backend.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        servers = {
                @Server(
                        url = "https://back-seuma-ede9exbagychaabf.eastus-01.azurewebsites.net",
                        description = "Azure Develop Server"
                ),
                @Server(
                        url = "http://localhost:3020",
                        description = "Local Development Server"
                )
        }
)
@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                final String securitySchemeName = "bearerAuth";

                return new OpenAPI().components(new Components().addSecuritySchemes(
                                securitySchemeName,
                                new SecurityScheme()
                                                .name("Authorization")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")))
                                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
        }
}