package pl.exaco.receiptApi.helpers

import org.apache.commons.text.StringEscapeUtils
import org.springframework.stereotype.Service
import pl.exaco.receiptApi.configuration.AppConfig
import pl.exaco.receiptApi.const.*
import pl.exaco.receiptApi.extensions.replaceIfStartsWith
import pl.exaco.receiptApi.extensions.substringBetween
import pl.exaco.receiptApi.extensions.swap
import pl.exaco.receiptApi.logger.Logger.Companion.logger
import pl.exaco.receiptApi.model.auth.CustomerCard
import pl.exaco.receiptApi.model.dto.*
import pl.exaco.receiptApi.model.external.ProductViewMixin
import pl.exaco.receiptApi.model.transaction.DiscountType
import pl.exaco.receiptApi.model.transaction.MobileItem
import pl.exaco.receiptApi.model.transaction.TransactionForMobile
import java.math.BigDecimal
import java.net.URLDecoder
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@Service
class ReceiptApiHelper(private val appConfig: AppConfig) {

    companion object {
        fun getSolrArticleName(solrArticle: String?): String {
            return solrArticle?.replace(solrArticle.substringAfterLast(solrStringSeparator) ?: "","")?.replace(getSolrArticlePrice(solrArticle),"")?.dropLast(2) ?: ""
        }

        fun getSolrArticleBarcode(solrArticle: String?): String {
            return solrArticle?.substringAfterLast(solrStringSeparator)?.replaceIfStartsWith(fiveZerosForEforceRequest) ?: ""
        }

        fun getSolrArticlePrice(solrArticle: String?): String {
            return solrArticle?.substringBetween(solrStringSeparator) ?: ""
        }
    }

    fun getEForceProductsForRequest(list: List<ReceiptSolrModel>?, articleName: String?): Set<String> {
        val eForceBarcodeSetToMakeRequest = mutableSetOf<String>()
        list?.forEach { receiptSolrModel ->
            receiptSolrModel.items = receiptSolrModel.items?.sortedByDescending {
                val itReplaced = StringEscapeUtils.unescapeHtml4(it)
                getSolrArticlePrice(itReplaced).toBigDecimal()
            }
            if (articleName != null) {
                try {
                    receiptSolrModel.items = receiptSolrModel.items?.toMutableList()?.swap(articleName)
                } catch (e: Exception) {
                    logger.info("Items Swap error: ${e.message}")
                }
            }
            var listSize = receiptSolrModel.items?.size
            if (listSize != null && listSize > 6) {
                listSize = 6
            }
            receiptSolrModel.items = listSize?.let { receiptSolrModel.items?.subList(0, it) }
            eForceBarcodeSetToMakeRequest.addAll(receiptSolrModel.items ?: emptyList())
        }
        return eForceBarcodeSetToMakeRequest
    }

    fun prepareReceiptBasicItemList(items: List<String>, eForceProductsMap: Map<String, ProductViewMixin>): List<ReceiptBasicItem> {
        return items.map {
            val itReplaced = StringEscapeUtils.unescapeHtml4(it)
            val eForceProduct = eForceProductsMap[getSolrArticleBarcode(itReplaced)]
            val articleUrl = prepareImageUrl(eForceProduct?.defaultImage?.name, false)
            val categoryUrl = prepareImageUrl(eForceProduct?.productCategories?.get(0)?.category?.parent?.parent?.files?.webIcon, true)
            val articleName = getSolrArticleName(itReplaced)
            val price = getSolrArticlePrice(itReplaced).toBigDecimal()
            ReceiptBasicItem(articleName, articleUrl, categoryUrl, price)
        }
    }

    fun prepareImageUrl(file: String?, category: Boolean): String? {
        return if (file.isNullOrBlank()) {
            null
        } else {
            if (category) {
                "${appConfig.eforce?.eForceCategoryUrl}$file"
            } else {
                "${appConfig.eforce?.eForceImageUrl}$file"
            }
        }
    }

    fun translateDiscountType(type: DiscountType?): String {
        return type?.translationPL ?: defaultDiscountType
    }

    fun getArticleNameForConverter(mongoName: String?, solrName: String?, item: MobileItem?): String {
        var name = mongoName
            ?: solrName
            ?: item?.articleName ?: "Brak nazwy artykułu"
        while (!name.first().isLetter()) {
            name = name.replaceFirst(name.first().toString(), "")
        }
        return name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    fun getDecodedArticleName(articleName: String?): String? {
        return if (!articleName.isNullOrBlank()) {
            URLDecoder.decode(articleName, Charsets.UTF_8.displayName())
        } else {
            articleName
        }
    }

    fun preparePaymentSet(mongoData: TransactionForMobile): Set<ReceiptPayment>? {
        return mongoData.payments?.map {
            ReceiptPayment(
                type = it.type ?: 0,
                typeName = PaymentType.getNameById(it.type),
                amount = if (it.type == 0)
                    (it.amount?.toBigDecimal()
                        ?: BigDecimal.ZERO).minus(mongoData.changeAmount?.toBigDecimal()
                        ?: BigDecimal.ZERO)
                else it.amount?.toBigDecimal() ?: BigDecimal.ZERO,
                currencyRate = BigDecimal.ZERO,  // Temporary returns 0
                overPayment = it.overPayment?.toBigDecimal() ?: BigDecimal.ZERO
            )
        }?.toSet()
    }

    fun getPreparedFiledQuery(cards: Set<CustomerCard>): String {
        return if (cards.size == 1) {
            "$loyaltyCardNumber:${cards.first().cardNumber}"
        } else {
            "$loyaltyCardNumber:(${cards.joinToString(orOperator) { it.cardNumber }})"
        }
    }

    fun getTotalDiscount(list: List<MobileItem>?): BigDecimal? {
        val sum = list?.map { it.discountAmount?.toBigDecimal() ?: 0.00.toBigDecimal() }?.fold(BigDecimal.ZERO, BigDecimal::add)
        if (sum?.compareTo(BigDecimal.ZERO) == 0) return null
        return sum
    }

    fun getTotalDateTime(solrModel: ReceiptSolrModel): ZonedDateTime {
        kotlin.runCatching {
            return if (solrModel.timeStamp != null && solrModel.timeStamp?.isNotEmpty() == true)
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(solrModel.timeStamp?.first()!!), ZoneId.systemDefault()) else
                ZonedDateTime.ofInstant(solrModel.totalDateTime?.toInstant(),  ZoneId.systemDefault())
        }.onFailure {
            logger.error("getTotalDateTime Exception: $it")
        }
        return ZonedDateTime.now()
    }

}