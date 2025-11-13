package pl.exaco.receiptApi.model.transaction

data class MobilePayment(
        var type: Int?,
        var amount: String?,
        var currencyRate: String? = null,
        var overPayment: String? = null
)

