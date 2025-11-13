package pl.exaco.receiptApi.model.transaction

data class MobileInfoLoyalty(
        var customerId: String?,
        var cardNumber: String?,
        var mobile: Boolean?,
        var pointsGranted: Int? = null,
        var pointsBurned: Int? = null
) 

