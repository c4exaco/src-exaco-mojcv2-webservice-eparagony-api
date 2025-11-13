package pl.exaco.receiptApi.model.transaction

data class MobileInfoCoupons(
        var cas: List<MobileInfoCouponsCasCoupon>? = null,
        var synerise: List<MobileInfoCouponsSyneriseCoupon>? = null
) 

