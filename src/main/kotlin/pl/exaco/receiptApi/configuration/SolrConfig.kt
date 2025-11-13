package pl.exaco.receiptApi.configuration

import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class SolrConfig(
    private val appConfig: AppConfig
) {

    @Bean
    fun solrClient(): SolrClient {
        val solrHostUrl = appConfig.eparagony?.api?.solr?.host
            ?: throw IllegalStateException("Brak konfiguracji 'solr.host' w AppConfig")

        println("✅ Konfiguracja klienta Solr (HttpSolrClient) dla hosta: $solrHostUrl")

        return HttpSolrClient.Builder(solrHostUrl)
            .withConnectionTimeout(10000, TimeUnit.MILLISECONDS)
            .build()
    }
}