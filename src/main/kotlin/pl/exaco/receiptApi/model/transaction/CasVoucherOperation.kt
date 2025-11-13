package pl.exaco.receiptApi.model.transaction

enum class CasVoucherOperation(val value: String) {

    ISSUE("ISSUE"),
    USAGE("USAGE"),
    OFFLINE("OFFLINE"),
    NOTUSED("NOTUSED");

}

