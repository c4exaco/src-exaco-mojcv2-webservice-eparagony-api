package pl.exaco.receiptApi.model.transaction

data class MobileItem(
        var lineNumber: Int?,
        var articleName: String?,
        var articleId: String?,
        var articleIndex: String?,
        var amount: String?,
        var quantity: String?,
        var basePrice: String?,
        var evidPrice: String?,
        var salePrice: String?,
        var discountAmount: String?,
        var tax: MobileItemTax?,
        var unit: String?,
        var inputType: ArticleInputType?,
        var discounts: List<MobileItemDiscount>?,
        var barcode: String? = null,
        var returnedQuantity: String? = null,
        var invoiceNumber: String? = null,
        var vat: String? = null,
        var articleType: ArticleType? = null,
        var articleSubtype: ArticleSubtype? = null
) {
        constructor(discountAmount: String?) : this(null, null,null,null,
                null,null,null,null,null, discountAmount,null,null,
                null,null,null,null,null,null,null,
                null)

        constructor(articleName: String?, discountAmount: String?) : this(null, articleName,null,null,
                null,null,null,null,null, discountAmount,null,null,
                null,null,null,null,null,null,null,
                null)
}