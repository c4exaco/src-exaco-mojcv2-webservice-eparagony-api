package pl.exaco.receiptApi.model.transaction

data class MobileItemDiscount(
        var source: ArticleDiscountSource?,
        var type: DiscountType?,
        var amount: String?,
        var quantity: String?,
        var promotionId: Int? = null,
        var promotionLevel: Int? = null
)