package pl.exaco.receiptApi.model.transaction

import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class TransactionForMobile(
        @field:Id
        var barcode: String?,
        var storeId: String?,
        var posId: Int?,
        var transactionNo: Int?,
        var posType: PosType?,
        var beginDateTime: LocalDateTime?,
        var totalDateTime: LocalDateTime?,
        var endDateTime: LocalDateTime?,
        var totalAmount: String?,
        var items: List<MobileItem>?,
        var payments: List<MobilePayment>?,
        var changeAmount: String? = null,
        var invoices: List<MobileInvoice>? = null,
        var info: MobileInfo? = null,
        var merchantNip: String? = null
) {
        constructor(totalAmount: String, payments: List<MobilePayment>?, changeAmount: String?) : this(null,
                null, null, null, null,null, null,
                null, totalAmount, null, payments, changeAmount, null, null, null)
}