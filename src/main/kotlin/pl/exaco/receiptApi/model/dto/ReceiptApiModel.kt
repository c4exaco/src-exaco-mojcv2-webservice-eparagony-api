package pl.exaco.receiptApi.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import org.apache.solr.client.solrj.beans.Field
// Usunięto import: org.springframework.data.solr.core.mapping.SolrDocument
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.*

open class ReceiptSolrModel {
        constructor()
        constructor(totalDateTime: Date?, timeStamp: List<Long>?) {
                this.totalDateTime = totalDateTime
                this.timeStamp = timeStamp
        }
        @Field(value = "id")
        open var id: String? = null
        @Field(value = "loyaltyCardNumber")
        open var loyaltyCardNumber: String? = null
        @Field(value = "totalAmount")
        open var totalAmount: Double? = null
        @Field(value = "items")
        open var items: List<String>? = null
        @Field(value = "articlesAmount")
        open var articlesAmount: Int? = null
        @Field(value = "storeId")
        open var storeId: String? = null
        @Field(value = "totalDateTime")
        open var totalDateTime: Date? = null
        @Field(value = "timeStamp")
        open var timeStamp: List<Long>? = null
}

data class ReceiptApiBasicModel(
        val receiptBarcode: String,
        val loyaltyCardNumber: String,
        val totalAmount: BigDecimal,
        val itemsAmount: Int,
        val items: List<ReceiptBasicItem>,
        @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.ZZ")
        val totalDateTime: ZonedDateTime,
        val store: ReceiptBasicStore)

data class ReceiptBasicItem(
        val articleName: String,
        val articleUrl: String?,
        val categoryUrl: String?,
        val price: BigDecimal)

data class ReceiptBasicStore(
        val storeId: String,
        val halabardId: Int,
        val street: String)

data class ReceiptApiModel(
        val receiptBarcode: String? = null,
        val store: ReceiptStore? = null,
        @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.ZZ")
        val totalDateTime: ZonedDateTime? = null,
        val receiptTitle: String? = null,
        val items: List<ReceiptItem>? = null,
        val tax: ReceiptTax? = null,
        val totalAmount: BigDecimal? = null,
        val paymentTypes: Set<ReceiptPayment>? = null,
        val cardNumber: String? = null,
        val customerTaxNumber: String? = null,
        val loyaltyCardNumber: String? = null,
        val pointsGranted: Int? = null,
        val pointsBurned: Int? = null,
        val pawnAmount: BigDecimal? = null,
        val receiptClause: String? = null,
        var receiptTotalDiscount: BigDecimal? = null
)

data class ReceiptStore(
        val storeId: String?,
        val storeTitle: String,
        val halabardId: Int,
        val postalCode: String,
        val city: String,
        val street: String,
        val taxNumber: String?
)

data class ReceiptItem(
        val articleName: String,
        val quantity: BigDecimal,
        val evidprice: BigDecimal,
        val amount: BigDecimal,
        val receiptItemDiscount: List<ReceiptItemDiscount>?,
        val returnedQuantity: BigDecimal?

)

data class ReceiptItemDiscount(
        val type: String?,
        val evidPrice: BigDecimal?,
        val amount: BigDecimal?,
        val quantity: BigDecimal?
)

data class ReceiptTax(
        val taxAmounts: Map<String, BigDecimal>,
        val taxPTU: BigDecimal
)

data class ReceiptPayment(
        val type: Int,
        val typeName: String,
        val amount: BigDecimal,
        val currencyRate: BigDecimal,
        var overPayment: BigDecimal
)

enum class SortField(val value: String) {
        TOTAL_DATE_TIME("totalDateTime"),
        TOTAL_AMOUNT("totalAmount"),
        ARTICLES_AMOUNT("articlesAmount"),
        STORE_ADDRESS("storeAddressFirstLetterIndex");
}

enum class PaymentType(val id: Int, val typeName: String) {
        CASH(0, "Gotówka"),
        C4_VOUCHER(1, "Bon CARREFOUR"),
        CARD(2, "Karta Płatnicza"),
        KOBA_BON(3, "Bon KOBA"),
        SODEXHO_BON_DISCOUNT(4, "Bon Sodexho Rabat"),
        SODEXHO_BON(5, "Bon Sodexho"),
        FOOD_VAUCHER(6, "Bon Wasza Żywieniowa"),
        DELAYED_PAYMENT(7, "Płatność Opóźniona"),
        MANUAL_CARD(8, "Karta Ręczna"),
        CARREFOUR_VISA(9, "CARREFOUR VISA"),
        OTHER_PAYMENT(10, "Inne Płatności"),
        EURO(11, "Euro"),
        AMEX_CARDS(12, "Karty AMEX"),
        BONUS_BON(13, "Bon Bonus"),
        VAUCHER_COUPONS(14, "Kupony Rabatowe"),
        LUKAS(15, "KREDYT LUKAS RATY"),
        SUBIEKT_BON(16, "BON SUBIEKT"),
        E_BON(17, "E-BON"),
        MILLENNIUM_CARD(18, "KARTA MILLENNIUM"),
        AURA_CARD(19, "Karta AURA"),
        BNA(20, "BNA Wpłatomat Tankomatu [Hectronic, PCS]"),
        HIPERC_CARD(21, "Karta HiperC MC"),
        C4_GIFT_CARD(22, "Karta Podarunkowa Carrefour"),
        VISA_MANUAL(23, "K.CARREFOUR VISA RĘCZNA"),
        VISA_VAUCHER(24, "K.CARREFOUR VISA Kupon"),
        C4_FLEET(25, "Karta Flota Carrefour"),
        BON_CARD(26, "BonCard"),
        ONLINE_CARD(27, "Płatność kartą online"),
        TPAY(28, "Tpay"),
        RATE_SALE(29, "Sprzedaż ratalna"),
        ECOMMERCE_VAUCHER(30, "Rabat eCommerce"),
        MOBILE_PAYMENT(31, "Płatność Mobilna"),
        HI_PAY(32, "Płatność HiPay"),
        TAX_FREE(33, "Gotówka TaxFree"),
        CF_MASTERCARD(34, "CF MasterCard"),
        CF_MASTERCARD_MANUAL(35, "CF MasterCard RĘCZNA"),
        GKH(37, "Bon GKH"),
        BANK_TRANSFER(38, "Przelew"),
        MOBILE_PAYMENT_CF_MASTERCARD(39, "Płatność Mobilna CFMasterCard"),
        FIRST_DATA(40, "First Data"),
        PAYU(41, "PayU");

        companion object {
                private val map: MutableMap<Int, PaymentType> = HashMap()
                init {
                        for (i in values()) {
                                map[i.ordinal] = i
                        }
                }
                fun getNameById(id: Int?): String {
                        return map[id]?.typeName ?: "Inne Płatności"
                }
        }
}