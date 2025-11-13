package pl.exaco.receiptApi.model.transaction

enum class CasCouponOperation(val value: String) {

    ISSUE("ISSUE"),
    USAGE("USAGE"),
    OFFLINE("OFFLINE"),
    NOTUSED("NOTUSED");

}

