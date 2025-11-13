package pl.exaco.receiptApi.connector.eforce

import org.springframework.stereotype.Service
import pl.exaco.receiptApi.model.auth.CustomerCard
import pl.exaco.receiptApi.model.external.ProductViewMixin

@Service
class EForceConnector(
    val eForceMultiArticleRequestExecutor: EForceMultiArticleRequestExecutor,
    val eForceMultiOver50ArticleRequestExecutor: EForceMultiOver50ArticleRequestExecutor
) {

    @Throws(Exception::class)
    fun getEForceArticles(barcodes: Set<String>, customerCards: Set<CustomerCard>): Map<String, ProductViewMixin> {
        return if (barcodes.size <= 50) {
            executeRequest(eForceMultiArticleRequestExecutor, customerCards, barcodes)
        } else {
            executeRequest(eForceMultiOver50ArticleRequestExecutor, customerCards, barcodes)
        }
    }

    fun executeRequest(
        requestExecutor: IEForceRequestExecutor,
        customerCards: Set<CustomerCard>,
        barcodes: Set<String>
    ): Map<String, ProductViewMixin> {
        return requestExecutor.executeEforceRequest(customerCards, barcodes)
    }

}