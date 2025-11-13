package pl.exaco.receiptApi.connector.eforce

import pl.exaco.receiptApi.model.auth.CustomerCard
import pl.exaco.receiptApi.model.external.ProductViewMixin

interface IEForceRequestExecutor {
    fun executeEforceRequest(customerCards: Set<CustomerCard>, barcodes: Set<String>): Map<String, ProductViewMixin>
}