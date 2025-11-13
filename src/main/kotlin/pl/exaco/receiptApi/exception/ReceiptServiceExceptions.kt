package pl.exaco.receiptApi.exception

data class AuthException(override val message: String?) : RuntimeException()

data class ReceiptNotFoundException(val barcode: String?) : RuntimeException()

data class ReceiptModelPrepareException(val exMessage: String?) : RuntimeException()

data class UposException(val exMessage: String?) : RuntimeException()

data class UposGetCardException(val exMessage: String?) : RuntimeException()

data class UposCustomerAccountDetailsException(val exMessage: String?) : RuntimeException()