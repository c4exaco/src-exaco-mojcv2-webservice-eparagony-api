package pl.exaco.receiptApi.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class SecretManagerConfig {

    @Bean
    @Profile("labo")
    fun appSecretsJsonFromGcpLabo(
        @Value("\${sm://projects/mojcv2-labo/secrets/tf-general-secret/versions/latest}")
        secretPayload: String
    ): AppConfig {
        println("✅ Loading secrets from GCP Secret Manager.")
        val objectMapper = ObjectMapper().registerKotlinModule()
        return objectMapper.readValue(secretPayload, AppConfig::class.java)
    }

    @Bean
    @Profile("prod")
    fun appSecretsJsonFromGcpProd(
        @Value("\${sm://projects/mojcv2-prod/secrets/tf-general-secret/versions/latest}")
        secretPayload: String
    ): AppConfig {
        println("✅ Loading secrets from GCP Secret Manager.")
        val objectMapper = ObjectMapper().registerKotlinModule()
        return objectMapper.readValue(secretPayload, AppConfig::class.java)
    }

    @Bean
    @Profile("local")
    fun appSecretsJsonFromFile(
        @Value("\${sm://projects/mojcv2-labo/secrets/tf-general-secret-local/versions/latest}")
        secretPayload: String
    ): AppConfig {
        println("✅ Loading secrets from local JSON file.")
        val objectMapper = ObjectMapper().registerKotlinModule()
        return objectMapper.readValue(secretPayload, AppConfig::class.java)
    }

}
