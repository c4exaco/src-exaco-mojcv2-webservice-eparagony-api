package pl.exaco.receiptApi.model.transaction

data class MobileInfo(
        var loyalty: MobileInfoLoyalty? = null,
        var cashbackWorld: String? = null,
        var kdrCardNumber: String? = null,
        var selfScanning: MobileInfoSelfScaning? = null,
        var clickAndCollect: MobileInfoClickAndCollect? = null,
        var nonFiscalTransactionNumber: String? = null,
        var customerNip: String? = null,
        var coupons: MobileInfoCoupons? = null,
        var vouchers: MobileInfoVouchers? = null
) 

