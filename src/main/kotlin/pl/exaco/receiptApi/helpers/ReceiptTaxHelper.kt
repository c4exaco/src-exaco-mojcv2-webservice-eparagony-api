package pl.exaco.receiptApi.helpers

import org.springframework.stereotype.Service
import pl.exaco.receiptApi.const.percentSign
import pl.exaco.receiptApi.const.ptu
import pl.exaco.receiptApi.const.solrStringSeparator
import pl.exaco.receiptApi.extensions.substringBeforeInc
import pl.exaco.receiptApi.logger.Logger.Companion.logger
import pl.exaco.receiptApi.model.dto.ReceiptTax
import pl.exaco.receiptApi.model.transaction.ArticleType
import pl.exaco.receiptApi.model.transaction.MobileItem
import pl.exaco.receiptApi.model.transaction.TransactionForMobile
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class ReceiptTaxHelper {
    fun prepareTax(mongoData: TransactionForMobile): ReceiptTax? {
        return kotlin.runCatching {

            val itemsWithNoPawn = mongoData.items?.filterNot { it.articleType?.equals(ArticleType.PAWN) ?: false }

            val taxTypeAmounts = indicateTaxElements(itemsWithNoPawn)

            ReceiptTax(
                    taxAmounts = sortTaxTypeAmounts(taxTypeAmounts[taxTypeAmounts.keys.first()]?.toMap()!!),
                    taxPTU = taxTypeAmounts.keys.first()
            )

        }.onFailure {
            logger.warn("Tax preparation failed for barcode: ${mongoData.barcode} with: ${it.message}")
        }.getOrNull()
    }

    fun indicateTaxElements(itemsWithNoPawn: List<MobileItem>?): MutableMap<BigDecimal,MutableMap<String, BigDecimal>> {
        val taxTypeAmounts = mutableMapOf<String, BigDecimal>()
        var taxPtu = BigDecimal.ZERO
        itemsWithNoPawn?.forEach { item ->
            val keyPtu = "${item.tax?.symbol!!}A;Sprzed. opod. $ptu ${item.tax?.symbol!!}"
            val key = "${item.tax?.symbol!!}B;Kwota ${item.tax?.symbol!!} ${item.tax?.value!!}$percentSign"
            if (taxTypeAmounts.containsKey(key)) {
                kotlin.runCatching {
                    taxTypeAmounts[keyPtu] = taxTypeAmounts[keyPtu]?.plus((item.amount?.toBigDecimal())!!)!!
                    taxTypeAmounts[key] = taxTypeAmounts[key]?.plus(getItemLineValue(item))!!
                }.onFailure { ex ->
                    logger.warn("Tax type map sum exception $ex")
                }
            } else {
                kotlin.runCatching {
                    taxTypeAmounts[keyPtu] = item.amount?.toBigDecimal()!!
                    taxTypeAmounts[key] = getItemLineValue(item)
                }
            }
        }
        val returnMap = mutableMapOf<String, BigDecimal>()
        taxTypeAmounts.forEach {
               returnMap[it.key] =
                   if (it.key.contains(ptu))
                       it.value
                   else
                       getVatLineForRate(it.value.setScale(2, RoundingMode.HALF_UP), getRate(it.key))
               taxPtu = taxPtu.add(
                   if (it.key.contains(ptu))
                       BigDecimal.ZERO
                   else
                       getVatLineForRate(it.value.setScale(2, RoundingMode.HALF_UP), getRate(it.key)))
        }
            return mutableMapOf(Pair(taxPtu,returnMap))
    }

    fun getItemLineValue(item: MobileItem): BigDecimal {
        val itemVat = item.salePrice?.toBigDecimal()
            ?.multiply(item.quantity?.toBigDecimal())
        return itemVat!!
    }

    fun getVatLineForRate(value: BigDecimal, rate: BigDecimal): BigDecimal {
        return value.multiply(rate).divide(BigDecimal.valueOf(100L).add(rate), 4, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP)
    }

    fun getRate(s: String): BigDecimal {
        return s.substringAfterLast(" ").substringBefore(percentSign).toBigDecimal()
    }

    fun sortTaxTypeAmounts(taxTypeAmounts: Map<String, BigDecimal>): Map<String, BigDecimal> {
        val sortedMap: MutableMap<String, BigDecimal> = LinkedHashMap()
        taxTypeAmounts.entries.sortedBy {
            it.key
        }.forEach {
            sortedMap[it.key.replace(it.key.substringBeforeInc(solrStringSeparator), "")] = it.value
        }
        return sortedMap
    }

    fun preparePawnAmount(items: List<MobileItem>?): BigDecimal? {
        val pawns = items?.filter { it.articleType?.equals(ArticleType.PAWN) ?: false }
        if (pawns.isNullOrEmpty()) {
            return null
        }
        return pawns.map { it.quantity!!.toBigDecimal().multiply(it.evidPrice!!.toBigDecimal()) }.fold(BigDecimal.ZERO, BigDecimal::add)
    }

}