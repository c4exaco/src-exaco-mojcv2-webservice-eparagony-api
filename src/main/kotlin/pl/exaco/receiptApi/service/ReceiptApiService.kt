package pl.exaco.receiptApi.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import pl.exaco.receiptApi.connector.eforce.EForceConnector
import pl.exaco.receiptApi.connector.upos.UposService
import pl.exaco.receiptApi.const.fiveZerosForEforceRequest
import pl.exaco.receiptApi.const.receiptClause
import pl.exaco.receiptApi.const.receiptTitle
import pl.exaco.receiptApi.dao.ReceiptApiDao
import pl.exaco.receiptApi.exception.ReceiptModelPrepareException
import pl.exaco.receiptApi.extensions.replaceIfStartsWith
import pl.exaco.receiptApi.helpers.ReceiptApiHelper
import pl.exaco.receiptApi.helpers.ReceiptApiHelper.Companion.getSolrArticleBarcode
import pl.exaco.receiptApi.helpers.ReceiptApiHelper.Companion.getSolrArticleName
import pl.exaco.receiptApi.helpers.ReceiptTaxHelper
import pl.exaco.receiptApi.logger.Logger.Companion.logger
import pl.exaco.receiptApi.model.IntranetStore
import pl.exaco.receiptApi.model.dto.*
import pl.exaco.receiptApi.model.transaction.ArticleType
import pl.exaco.receiptApi.model.transaction.MobileItemDiscount
import pl.exaco.receiptApi.model.transaction.TransactionForMobile
import java.math.BigDecimal
import java.math.RoundingMode


@Service
class ReceiptApiService(private val receiptApiDao: ReceiptApiDao,
                        private val eForceConnector: EForceConnector,
                        private val receiptApiHelper: ReceiptApiHelper,
                        private val receiptTaxHelper: ReceiptTaxHelper,
                        private val uposService: UposService
) {
    fun getCustomerReceiptList(customerId: Int,
                               articleSearchName: String?,
                               dateFrom: String?,
                               dateTo: String?,
                               storeId: String?,
                               totalFrom: BigDecimal?,
                               totalTo: BigDecimal?,
                               sortField: SortField?,
                               direction: Sort.Direction?,
                               pageNo: String?,
                               pageSize: String?,
                               loyaltyCardNumber: String): Pair<Page<ReceiptApiBasicModel>, String> {

        val articleName = receiptApiHelper.getDecodedArticleName(articleSearchName)

        val cards = uposService.getActualCustomerCards(customerId)

        val returnModelRaw = receiptApiDao.getByLoyaltyCardNumber(
            cards = cards,
            dateFrom = dateFrom,
            dateTo = dateTo,
            storeId = storeId,
            totalFrom = totalFrom,
            totalTo = totalTo,
            sortField = sortField,
            direction = direction,
            pageNo = pageNo,
            pageSize = if (!articleName.isNullOrBlank()) "100" else pageSize
        )

        val returnModel: Page<ReceiptSolrModel>? = if (!articleName.isNullOrBlank() && returnModelRaw?.content?.isNotEmpty() == true) {
            var count = 0
            val modifiedAppList = mutableListOf<ReceiptSolrModel>()
            returnModelRaw?.content?.forEach {
                if (it.items?.any { item -> item.lowercase().contains(articleName.lowercase()) } == true && count < (pageSize?.toInt() ?: 10)) {
                    modifiedAppList.add(it)
                    count++
                }
            }
            val newPage = PageImpl(modifiedAppList, PageRequest.of(returnModelRaw?.number ?: 0, returnModelRaw?.totalPages ?: 10), modifiedAppList.size.toLong())
            newPage
        } else {
            returnModelRaw
        }

        val receiptsItemsSizeBeforeReduceAction = returnModel?.content?.associate { it.id to it.articlesAmount }

        val eanSetForEForceRequest = receiptApiHelper.getEForceProductsForRequest(
                returnModel?.content?.toList(),
                articleName
        ).map { getSolrArticleBarcode(it) }.toSet()

        val eForceProductsMap = eForceConnector.getEForceArticles(eanSetForEForceRequest, cards)

        val page = returnModel?.map {
            val items = receiptApiHelper.prepareReceiptBasicItemList(it.items ?: emptyList(), eForceProductsMap)
            ReceiptApiBasicModel(
                receiptBarcode = it.id!!,
                loyaltyCardNumber = it.loyaltyCardNumber!!,
                totalAmount = it.totalAmount!!.toBigDecimal(),
                itemsAmount = receiptsItemsSizeBeforeReduceAction?.get(it.id) ?: 0,
                totalDateTime = receiptApiHelper.getTotalDateTime(it),
                items = items,
                store = if (receiptApiDao.getStore(it.storeId!!).isPresent) receiptApiDao.getStore(it.storeId!!).get().let { store ->
                        ReceiptBasicStore(
                                store.storeId!!, store.halabardId, store.street)
                    } else ReceiptBasicStore("", 0, "")
            )
        }
        val totalDiscount = getTotalDiscount(loyaltyCardNumber, customerId)
        logger.info("Getting receipt list for loyalty card no: ${cards.joinToString(", ") { it.cardNumber }}" +
                " - List of: ${returnModel?.content?.size} items. Request: ArticleName = ${articleName?.trim()}," +
                " DateFrom = $dateFrom, DateTo = $dateTo, Pageable = sortField: $sortField, direction: $direction," +
                " pageNo: $pageNo, pagesize: $pageSize, ${page?.content?.joinToString { it.toString() }}")
        return Pair(page!!, totalDiscount)
    }

    fun getByBarcode(receiptBarcode: String, customerId: String): ReceiptApiModel? {
        logger.info("Getting receipt by barcode: $receiptBarcode for customer: $customerId")
        val solrData = receiptApiDao.getSolrData(receiptBarcode)
        val mongoData = receiptApiDao.getMongoData(receiptBarcode)
        val storeData = receiptApiDao.getStoreData(solrData.storeId)
        return convertApiModel(storeData, mongoData, solrData)
    }

    fun getShopList(loyaltyCardNumber: String): Set<ReceiptBasicStore> {
        val storeIds = receiptApiDao.getReceiptsStoreIdsByCustomer(loyaltyCardNumber)
        val mongoStores = receiptApiDao.getMongoStoresByIds(storeIds)
        val shopList = mongoStores.map {
            val store = mongoStores.find { mongoStore ->
                mongoStore.storeId?.equals(it.storeId)!!
            }
            ReceiptBasicStore(it.storeId!!, store?.halabardId!!, store.street)
        }.toSet()
        if (shopList.isEmpty()) {
            logger.warn("Shoplist is empty for customer: $loyaltyCardNumber")
        }
        logger.info("Shoplist for customer: $loyaltyCardNumber received. Shoplist: $shopList")
        return shopList
    }

    private fun convertApiModel(storeData: IntranetStore?, mongoData: TransactionForMobile, solrData: ReceiptSolrModel): ReceiptApiModel? {
        return kotlin.runCatching {
            ReceiptApiModel(
                    receiptBarcode = mongoData.barcode,
                    store = ReceiptStore(storeId = storeData?.storeId,
                            storeTitle = storeData?.storeTitle!!,
                            halabardId = storeData.halabardId,
                            postalCode = storeData.postalCode,
                            city = storeData.city,
                            street = storeData.street,
                            taxNumber = mongoData.merchantNip),
                    totalDateTime = receiptApiHelper.getTotalDateTime(solrData),
                    receiptTitle = receiptTitle,
                    items = mongoData.items
                            ?.filterNot { it.articleType?.equals(ArticleType.PAWN) ?: false }
                            ?.map {
                                ReceiptItem(receiptApiHelper.getArticleNameForConverter(receiptApiDao.getEForceArticleByBarcode(it.barcode!!)?.articleName,
                                        getSolrArticleName(solrData.items?.first { sd -> getSolrArticleBarcode(sd) == it.barcode?.replaceIfStartsWith(fiveZerosForEforceRequest) }),
                                        it),
                                        it.quantity!!.toBigDecimal(),
                                        it.evidPrice!!.toBigDecimal(),
                                        it.quantity!!.toBigDecimal().multiply(it.evidPrice!!.toBigDecimal()),
                                        getAllDiscountsAsOnePosition(it.discounts, it.quantity, it.discountAmount),
                                        it.returnedQuantity?.toBigDecimal()
                                )
                            },
                    tax = receiptTaxHelper.prepareTax(mongoData),
                    totalAmount = solrData.totalAmount?.toBigDecimal(),
                    paymentTypes = receiptApiHelper.preparePaymentSet(mongoData),
                    cardNumber = null,
                    customerTaxNumber = mongoData.info?.customerNip,
                    loyaltyCardNumber = solrData.loyaltyCardNumber,
                    pointsBurned = mongoData.info?.loyalty?.pointsBurned,
                    pointsGranted = mongoData.info?.loyalty?.pointsGranted,
                    pawnAmount = receiptTaxHelper.preparePawnAmount(mongoData.items),
                    receiptClause = receiptClause,
                    receiptTotalDiscount = receiptApiHelper.getTotalDiscount(mongoData.items)
            )
        }.onFailure {
            logger.error(it.message)
            throw ReceiptModelPrepareException(it.message)
        }.onSuccess {
            logger.info("GetByBarcode success: $it")
        }.getOrNull()
    }

    fun getAllDiscountsAsOnePosition(discounts: List<MobileItemDiscount>?, itemQuantity: String?, discountAmount: String?): List<ReceiptItemDiscount> {
        var returnList = emptyList<ReceiptItemDiscount>()
        kotlin.runCatching {
            if (discounts.isNullOrEmpty()) return emptyList()

            if (discounts.any { it.quantity?.toBigDecimal() != itemQuantity?.toBigDecimal() }) {
                returnList = listOf(ReceiptItemDiscount(
                    type = receiptApiHelper.translateDiscountType(discounts.first().type),
                    evidPrice = discountAmount?.toBigDecimal(),
                    amount = discountAmount?.toBigDecimal(),
                    quantity = BigDecimal.ONE
                ))
            } else {
                val receiptItemDiscountList = discounts.map { dis ->
                    ReceiptItemDiscount(
                        type = receiptApiHelper.translateDiscountType(dis.type),
                        evidPrice = dis.amount?.toBigDecimal()?.divide(dis.quantity?.toBigDecimal(), 2, RoundingMode.HALF_UP),
                        amount = dis.amount?.toBigDecimal(),
                        quantity = dis.quantity?.toBigDecimal()
                    )
                }

                returnList = listOf(ReceiptItemDiscount(
                    type = receiptItemDiscountList.first().type,
                    evidPrice = receiptItemDiscountList.sumOf { it.evidPrice!!.toDouble() }.toBigDecimal().setScale(2, RoundingMode.HALF_UP),
                    amount = receiptItemDiscountList.sumOf { it.amount!!.toDouble() }.toBigDecimal().setScale(2, RoundingMode.HALF_UP),
                    quantity = receiptItemDiscountList.first().quantity

                ))
            }
        }.onFailure {
            returnList =  listOf(ReceiptItemDiscount(
                type = receiptApiHelper.translateDiscountType(discounts?.first()?.type),
                evidPrice = discountAmount?.toBigDecimal(),
                amount = discountAmount?.toBigDecimal(),
                quantity = BigDecimal.ONE
            ))
        }
        return returnList
    }

    fun getTotalDiscount(loyaltyCardNumber: String, customerId: Int): String {
        var totalDiscount = BigDecimal.ZERO
        kotlin.runCatching {
            val uposResponse = uposService.getCustomerAccountDetails(loyaltyCardNumber, customerId)
            totalDiscount = uposResponse?.discountAmount?.toBigDecimal() ?: BigDecimal.ZERO
        }.onFailure {
            totalDiscount = BigDecimal.ZERO
        }
        return totalDiscount.divide(BigDecimal.valueOf(100)).toString()
    }

}

