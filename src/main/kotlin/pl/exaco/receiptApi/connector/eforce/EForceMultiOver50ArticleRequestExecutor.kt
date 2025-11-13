package pl.exaco.receiptApi.connector.eforce

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import pl.exaco.receiptApi.configuration.AppConfig
import pl.exaco.receiptApi.logger.Logger.Companion.logger
import pl.exaco.receiptApi.model.auth.CustomerCard
import pl.exaco.receiptApi.model.external.ProductViewMixin
import java.net.URI

@Service
class EForceMultiOver50ArticleRequestExecutor(val appConfig: AppConfig,
                                              @Qualifier("EForceRestTemplate") private val restTemplate: RestTemplate):
    IEForceRequestExecutor {
    override fun executeEforceRequest(customerCards: Set<CustomerCard>, barcodes: Set<String>): Map<String, ProductViewMixin> {
        val aggregateResponse = mutableMapOf<String, ProductViewMixin>()
        barcodes.chunked(50).forEach {
            val stringList = it.joinToString(",")
            var response: MutableMap<String, ProductViewMixin> = mutableMapOf()
                try {
                    response =
                        restTemplate.exchange(
                            RequestEntity<Map<String, ProductViewMixin>>(HttpMethod.GET, URI("${appConfig.eforce?.eforceUrlMultiArticle}$stringList")),
                            object: ParameterizedTypeReference<Map<String, ProductViewMixin>>() {}
                        ).body?.toMutableMap()!!
                } catch (e: Exception) {
                    logger.warn("LoyaltyCardNumber[${customerCards.joinToString(", ") { card -> card.cardNumber }}] - EForce multi over 50 product request failed: $stringList -- ${e.message}")
                }
                aggregateResponse.putAll(response)
        }
        return aggregateResponse
    }
}