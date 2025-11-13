package pl.exaco.receiptApi.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class OpenApiConfig(private val environment: Environment) {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val server = when (environment.activeProfiles.firstOrNull()) {
            "labo" -> Server().url("http://192.168.11.16:8080/")
            "prod" -> Server().url("http://192.168.12.25:8080/")
            else -> Server().url("http://localhost:8080/")
        }
        val bearerAuth = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")

        return OpenAPI()
            .components(
                Components()
                    .addSecuritySchemes("bearerAuth", bearerAuth)
            )
            .addSecurityItem(
                SecurityRequirement()
                    .addList("bearerAuth")
            )
            .servers(
                listOf(
                    server
                )
            )
    }
}