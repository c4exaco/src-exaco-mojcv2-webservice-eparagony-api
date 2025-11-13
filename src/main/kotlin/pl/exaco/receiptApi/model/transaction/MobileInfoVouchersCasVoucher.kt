package pl.exaco.receiptApi.model.transaction

data class MobileInfoVouchersCasVoucher(
        var number: String?,
        var operation: CasVoucherOperation?,
        var amount: String? = null,
        var poolNumber: Int? = null,
        var promotionId: Int? = null,
        var promotionLevel: Int? = null
)

