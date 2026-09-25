package com.agentx.campus.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "AgentX Campus API",
        version = "1.0.0",
        description = "Multi-agent intelligent campus management system — REST API documentation. " +
            "Authenticate using the Authorize button with a JWT Bearer token obtained from " +
            "POST /api/v1/auth/login with {username, password}."
    ),
    servers = @Server(url = "http://localhost:8080", description = "Local Development Server")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Provide a JWT token. Obtain one from POST /api/v1/auth/login."
)
public class OpenApiConfig {
}
