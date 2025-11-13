package pl.exaco.receiptApi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import pl.exaco.receiptApi.configuration.AppConfig
import pl.exaco.receiptApi.connector.eforce.EForceConnector
import pl.exaco.receiptApi.connector.upos.CustomerAccountDetails
import pl.exaco.receiptApi.connector.upos.UposService
import pl.exaco.receiptApi.dao.ReceiptApiDao
import pl.exaco.receiptApi.exception.UposCustomerAccountDetailsException
import pl.exaco.receiptApi.helpers.ReceiptApiHelper
import pl.exaco.receiptApi.helpers.ReceiptTaxHelper
import pl.exaco.receiptApi.model.transaction.ArticleDiscountSource
import pl.exaco.receiptApi.model.transaction.DiscountType
import pl.exaco.receiptApi.model.transaction.MobileItemDiscount
import java.math.BigDecimal
import java.math.RoundingMode

internal class ReceiptApiServiceTest {

    private lateinit var receiptApiDao: ReceiptApiDao
    private lateinit var eForceConnector: EForceConnector
    private lateinit var receiptApiHelper: ReceiptApiHelper
    private lateinit var receiptTaxHelper: ReceiptTaxHelper
    private lateinit var uposService: UposService
    private lateinit var appConfig: AppConfig
    private lateinit var receiptApiService: ReceiptApiService

    @BeforeEach
    fun initializeEach() {
        receiptApiDao = Mockito.mock(ReceiptApiDao::class.java)
        appConfig = Mockito.mock(AppConfig::class.java)
        eForceConnector = Mockito.mock(EForceConnector::class.java)
        uposService = Mockito.mock(UposService::class.java)
        receiptTaxHelper = ReceiptTaxHelper()
        receiptApiHelper = ReceiptApiHelper(appConfig)
        receiptApiService = ReceiptApiService(receiptApiDao, eForceConnector, receiptApiHelper, receiptTaxHelper, uposService)
    }

    @Test
    fun getAllDiscountsAsOnePositionSameQuantityDiscount() {
        val dis1 = MobileItemDiscount(ArticleDiscountSource.SYNERISE, DiscountType.COUPON_ITEM, "1.00", "5", 1, 1)
        val dis2 = MobileItemDiscount(ArticleDiscountSource.SYNERISE, DiscountType.COUPON_ITEM, "5.00", "5", 1, 1)
        val dis3 = MobileItemDiscount(ArticleDiscountSource.SYNERISE, DiscountType.COUPON_ITEM, "2.00", "5", 1, 1)
        val dis4 = MobileItemDiscount(ArticleDiscountSource.SYNERISE, DiscountType.COUPON_ITEM, "5.43", "5", 1, 1)
        val mobileItemDiscount = listOf(dis1, dis2, dis3, dis4)

        val toOne = receiptApiService.getAllDiscountsAsOnePosition(mobileItemDiscount, "5", "13.43")
        Assertions.assertTrue(toOne.first().amount == BigDecimal.valueOf(13.43))
        Assertions.assertTrue(toOne.first().evidPrice == BigDecimal.valueOf(2.69))
        assertEquals(toOne.size, 1)
    }

    @Test
    fun getAllDiscountsAsOnePositionDifferentQuantityDiscount() {
        val dis1 = MobileItemDiscount(ArticleDiscountSource.SYNERISE, DiscountType.MANUAL_ITEM, "1.62", "1", 1, 1)
        val dis2 = MobileItemDiscount(ArticleDiscountSource.EUROPOS, DiscountType.PROMOTION_TRANSACTION, "0.81", "3", 1, 1)
        val dis3 = MobileItemDiscount(ArticleDiscountSource.EUROPOS, DiscountType.LOYALTY_TRANSACTION, "0.27", "3", 1, 1)
        val mobileItemDiscount = listOf(dis1, dis2, dis3)

        val toOne = receiptApiService.getAllDiscountsAsOnePosition(mobileItemDiscount, "3", "2.70")
        Assertions.assertTrue(toOne.first().amount == BigDecimal.valueOf(2.70).setScale(2, RoundingMode.HALF_UP))
        Assertions.assertTrue(toOne.first().evidPrice == BigDecimal.valueOf(2.70).setScale(2, RoundingMode.HALF_UP))
        Assertions.assertTrue(toOne.first().quantity == BigDecimal.ONE)
        assertEquals(toOne.size, 1)
    }

    @Test
    fun getAllDiscountsAsOnePosition_whenNoDiscounts_emptyList() {
        val mobileItemDiscount = null
        val toOne = receiptApiService.getAllDiscountsAsOnePosition(mobileItemDiscount, null, null)
        assertEquals(toOne.size, 0)
    }

    @Test
    fun getTotalDiscount_test_on_value() {
        val customerCard = "1234"
        val customerId = 1234

        Mockito.doReturn(CustomerAccountDetails(discountAmount = 12345)).`when`(uposService).getCustomerAccountDetails(anyString(), anyInt())
        val total = receiptApiService.getTotalDiscount(customerCard, customerId)
        assertEquals(total.toBigDecimal(), BigDecimal.valueOf(123.45))
    }

    @Test
    fun getTotalDiscount_test_on_0() {
        val customerCard = "1234"
        val customerId = 1234

        Mockito.doReturn(CustomerAccountDetails(discountAmount = 0)).`when`(uposService).getCustomerAccountDetails(anyString(), anyInt())
        val total = receiptApiService.getTotalDiscount(customerCard, customerId)
        assertEquals(total.toBigDecimal(), BigDecimal.ZERO)
    }

    @Test
    fun getTotalDiscount_test_on_null() {
        val customerCard = "1234"
        val customerId = 1234

        Mockito.doReturn(CustomerAccountDetails()).`when`(uposService).getCustomerAccountDetails(anyString(), anyInt())
        val total = receiptApiService.getTotalDiscount(customerCard, customerId)
        assertEquals(total.toBigDecimal(), BigDecimal.ZERO)
    }

    @Test
    fun getTotalDiscount_test_on_upos_exception() {
        val customerCard = "1234"
        val customerId = 1234

        Mockito.doThrow(UposCustomerAccountDetailsException("Błąd pobierania danych użytkownika")).`when`(uposService).getCustomerAccountDetails(anyString(), anyInt())
        val total = receiptApiService.getTotalDiscount(customerCard, customerId)
        assertEquals(total.toBigDecimal(), BigDecimal.ZERO)
    }
}