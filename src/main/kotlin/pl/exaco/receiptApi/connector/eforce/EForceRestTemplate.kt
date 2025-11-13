package pl.exaco.receiptApi.connector.eforce

import org.apache.http.HttpHeaders.ACCEPT
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import pl.exaco.receiptApi.configuration.AppConfig
import pl.exaco.receiptApi.logger.LoggingInterceptor

@Configuration
class EForceRestTemplate(val appConfig: AppConfig) {

    @Bean
    @Qualifier("EForceRestTemplate")
    fun template(): RestTemplate {
        return RestTemplateBuilder(RestTemplateCustomizer { rt: RestTemplate ->
            rt.interceptors.add(
                ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
                    request.headers.setBasicAuth(appConfig.eforce?.eforceUsername!!, appConfig.eforce?.eforcePassword!!)
                    request.headers.add(ACCEPT, MediaType.APPLICATION_JSON.type)
                    execution.execute(request, body!!)
                })
            rt.interceptors.add(LoggingInterceptor())
        }).build()
    }

}

