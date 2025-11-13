package pl.exaco.receiptApi.model.auth

import java.util.*

open class CustomerCard(
        open val cardNumber: String = "",
        open val cardTypeId: String = "",
        open val status: Short = 0,
        open val statusUpdateDateTime: Date? = null,
        open val customerId: Int = -1,
        open val insertDate: Date? = null,
        open val updateDate: Date? = null,
        open val lastBatchNumber: Int = 0
) {
        constructor(cardNumber: String) : this(cardNumber, "", 0, null, -1, null, null, 0)
}
