package pl.exaco.receiptApi.model.transaction

enum class ArticleInputType(val value: String) {

    PHOTO("PHOTO"),
    MANUAL("MANUAL"),
    SCANNER_EAN8("SCANNER_EAN8"),
    SCANNER_EAN13("SCANNER_EAN13"),
    SCANNER_UPC_A("SCANNER_UPC_A"),
    SCANNER_UPC_E("SCANNER_UPC_E"),
    SCANNER_CODE128("SCANNER_CODE128");

}

