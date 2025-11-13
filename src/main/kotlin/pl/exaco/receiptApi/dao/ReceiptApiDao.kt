package pl.exaco.receiptApi.dao

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import pl.exaco.receiptApi.const.*
import pl.exaco.receiptApi.exception.ReceiptNotFoundException
import pl.exaco.receiptApi.logger.Logger.Companion.logger
import pl.exaco.receiptApi.model.IntranetStore
import pl.exaco.receiptApi.model.auth.CustomerCard
import pl.exaco.receiptApi.model.dto.ReceiptSolrModel
import pl.exaco.receiptApi.model.dto.SortField
import pl.exaco.receiptApi.model.external.EForceArticle
import pl.exaco.receiptApi.model.transaction.TransactionForMobile
import pl.exaco.receiptApi.mongorepository.ReceiptApiEForceArticleRepository
import pl.exaco.receiptApi.mongorepository.ReceiptApiIntranetStoresRepository
import pl.exaco.receiptApi.mongorepository.ReceiptApiTransactionRepository
import pl.exaco.receiptApi.solrrepository.ReceiptApiSolrRepository
import java.math.BigDecimal
import java.util.*

@Repository
class ReceiptApiDao(
    private val receiptApiSolrRepository: ReceiptApiSolrRepository,
    private val receiptApiIntranetStoresRepository: ReceiptApiIntranetStoresRepository,
    private val receiptApiEForceArticleRepository: ReceiptApiEForceArticleRepository,
    private val receiptApiTransactionRepository: ReceiptApiTransactionRepository
) {

    fun getByLoyaltyCardNumber(
        cards: Set<CustomerCard>,
        dateFrom: String?,
        dateTo: String?,
        storeId: String?,
        totalFrom: BigDecimal?,
        totalTo: BigDecimal?,
        sortField: SortField?,
        direction: Sort.Direction?,
        pageNo: String?,
        pageSize: String?
    ): Page<ReceiptSolrModel> {

        val sort = if (sortField == null) {
            Sort.by(direction ?: Sort.Direction.DESC, SortField.TOTAL_DATE_TIME.value)
        } else {
            Sort.by(direction ?: Sort.Direction.ASC, sortField.value)
        }
        val pageable = PageRequest.of(pageNo?.toInt() ?: 0, pageSize?.toInt() ?: 20, sort)

        val fieldQuery = mutableListOf<String>()

        val cardsQuery = if (cards.size == 1) {
            "$loyaltyCardNumber:${cards.first().cardNumber}"
        } else {
            "$loyaltyCardNumber:(${cards.joinToString(orOperator) { it.cardNumber }})"
        }
        fieldQuery.add(cardsQuery)

        if (storeId != null) {
            fieldQuery.add("$storeIdFieldQuery:$storeId")
        }

        if (totalFrom != null || totalTo != null) {
            fieldQuery.add("${SortField.TOTAL_AMOUNT.value}:[${totalFrom ?: zero}$toOperator${totalTo ?: Int.MAX_VALUE.toString()}]")
        } else {
            fieldQuery.add("${SortField.TOTAL_AMOUNT.value}:[$zero$toOperator${Int.MAX_VALUE}]")
        }

        if (dateFrom != null || dateTo != null) {
            fieldQuery.add("${SortField.TOTAL_DATE_TIME.value}:[${dateFrom ?: nowMinusYear}$toOperator${dateTo ?: now}]")
        }

        return receiptApiSolrRepository.findUsingEdismax(
            fieldQuery = fieldQuery,
            queryField = defaultFiled,
            pageable = pageable,
            minimumShouldMatch = minimumShouldMatch
        )
    }

    fun getSolrData(receiptBarcode: String): ReceiptSolrModel {
        return receiptApiSolrRepository.findById(receiptBarcode).orElseThrow {
            logger.warn("No solr data for barcode: $receiptBarcode")
            throw ReceiptNotFoundException(receiptBarcode)
        }
    }

    fun getMongoData(receiptBarcode: String): TransactionForMobile {
        return receiptApiTransactionRepository.findByBarcode(receiptBarcode)
            ?: throw ReceiptNotFoundException(receiptBarcode)
    }

    fun getStoreData(storeId: String?): IntranetStore? {
        return storeId?.let { receiptApiIntranetStoresRepository.findById(it) }?.get()
    }

    fun getStore(storeId: String): Optional<IntranetStore> {
        return receiptApiIntranetStoresRepository.findById(storeId)
    }

    fun getEForceArticle(name: String): EForceArticle? {
        return receiptApiEForceArticleRepository.findByArticleName(name)
    }

    fun getEForceArticleByBarcode(barcode: String): EForceArticle? {
        return receiptApiEForceArticleRepository.findByBarcode(barcode)
    }

    fun getReceiptsStoreIdsByCustomer(loyaltyCardNumber: String): Set<String?> {
        return receiptApiSolrRepository.findByLoyaltyCardNumber(loyaltyCardNumber).map { it.storeId }.toSet()
    }

    fun getMongoStoresByIds(storeIds: Set<String?>): MutableIterable<IntranetStore> {
        return receiptApiIntranetStoresRepository.findAllById(storeIds)
    }
}