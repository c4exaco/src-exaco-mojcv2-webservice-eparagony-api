package pl.exaco.receiptApi.model.transaction

data class MobileInfoCouponsCasCoupon(
        var number: String?,
        var operation: CasCouponOperation?,
        var poolNumber: Int? = null,
        var promotionId: Int? = null,
        var promotionLevel: Int? = null
)