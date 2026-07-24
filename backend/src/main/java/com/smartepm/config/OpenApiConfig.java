package com.smartepm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes interactive API docs at /swagger-ui.html (or /swagger-ui/index.html)
 * and the raw OpenAPI spec at /v3/api-docs.
 *
 * Use the "Authorize" button in Swagger UI with a raw JWT (no "Bearer " prefix needed,
 * it's added automatically) obtained from POST /api/auth/login.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI smartEpmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Employee & Project Management System API")
                        .description("REST API for managing employees, projects, and tasks with JWT-based role authentication (ADMIN / EMPLOYEE).")
                        .version("v1.0.0")
                        .contact(new Contact().name("Smart EPM Team")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
