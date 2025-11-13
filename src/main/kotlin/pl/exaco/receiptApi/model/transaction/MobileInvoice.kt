package pl.exaco.receiptApi.model.transaction

import java.time.LocalDateTime

data class MobileInvoice(
        var number: String?,
        var datetime: LocalDateTime?
) 

