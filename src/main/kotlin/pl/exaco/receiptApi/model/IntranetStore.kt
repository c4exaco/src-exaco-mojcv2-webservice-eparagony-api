package pl.exaco.receiptApi.model

import org.springframework.data.annotation.Id

data class IntranetStore(
        @Id
        val storeId: String?,
        val storeTitle: String,
        val halabardId: Int,
        val postalCode: String,
        val city: String,
        val street: String,
        val customerTaxNumber: String?,
        val taxNumber: String?
)