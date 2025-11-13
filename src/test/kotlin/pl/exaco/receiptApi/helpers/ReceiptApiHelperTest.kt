package pl.exaco.receiptApi.dao

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.isNull
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import pl.exaco.receiptApi.configuration.AppConfig
import pl.exaco.receiptApi.const.*
import pl.exaco.receiptApi.model.auth.CustomerCard
import pl.exaco.receiptApi.model.dto.SortField
import pl.exaco.receiptApi.mongorepository.ReceiptApiEForceArticleRepository
import pl.exaco.receiptApi.mongorepository.ReceiptApiIntranetStoresRepository
import pl.exaco.receiptApi.mongorepository.ReceiptApiTransactionRepository
import pl.exaco.receiptApi.solrrepository.ReceiptApiSolrRepository
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
internal class ReceiptApiDaoTest {

    @Mock
    private lateinit var receiptApiSolrRepository: ReceiptApiSolrRepository
    @Mock
    private lateinit var receiptApiIntranetStoresRepository: ReceiptApiIntranetStoresRepository
    @Mock
    private lateinit var receiptApiEForceArticleRepository: ReceiptApiEForceArticleRepository
    @Mock
    private lateinit var receiptApiTransactionRepository: ReceiptApiTransactionRepository
    @Mock
    private lateinit var appConfig: AppConfig

    @InjectMocks
    private lateinit var receiptApiDao: ReceiptApiDao

    @Test
    fun `getByLoyaltyCardNumber builds correct query`() {
        val fieldQueryCaptor = ArgumentCaptor.forClass(List::class.java as Class<List<String>>)
        val queryFieldCaptor = ArgumentCaptor.forClass(String::class.java)
        val pageableCaptor = ArgumentCaptor.forClass(Pageable::class.java)
        val mmCaptor = ArgumentCaptor.forClass(String::class.java)

        `when`(receiptApiSolrRepository.findUsingEdismax(any(), any(), any(), any(), isNull()))
            .thenReturn(Page.empty())

        val cards = mutableSetOf(CustomerCard(cardNumber = "0012"))

        receiptApiDao.getByLoyaltyCardNumber(
            cards = cards,
            dateFrom = "2020-01-10T07:32:28.022Z",
            dateTo = "2020-01-10T07:32:28.022Z",
            storeId = "188",
            totalFrom = BigDecimal.ZERO,
            totalTo = BigDecimal.valueOf(100),
            sortField = SortField.TOTAL_AMOUNT,
            direction = Sort.Direction.DESC,
            pageNo = "0",
            pageSize = "10"
        )

        verify(receiptApiSolrRepository).findUsingEdismax(
            fieldQueryCaptor.capture(),
            queryFieldCaptor.capture(),
            pageableCaptor.capture(),
            mmCaptor.capture(),
            isNull()
        )

        val capturedFieldQuery = fieldQueryCaptor.value
        assertTrue(capturedFieldQuery.contains("loyaltyCardNumber:0012"))
        assertTrue(capturedFieldQuery.contains("storeId:188"))
        assertTrue(capturedFieldQuery.contains("totalAmount:[0 TO 100]"))
        assertTrue(capturedFieldQuery.contains("totalDateTime:[2020-01-10T07:32:28.022Z TO 2020-01-10T07:32:28.022Z]"))

        assertEquals(defaultFiled, queryFieldCaptor.value)
        assertEquals(minimumShouldMatch, mmCaptor.value)

        val capturedPageable = pageableCaptor.value
        assertEquals(0, capturedPageable.pageNumber)
        assertEquals(10, capturedPageable.pageSize)
        assertEquals(Sort.by(Sort.Direction.DESC, "totalAmount"), capturedPageable.sort)
    }

    @Test
    fun `getByLoyaltyCardNumber builds correct query for multiple cards`() {
        val fieldQueryCaptor = ArgumentCaptor.forClass(List::class.java as Class<List<String>>)
        `when`(receiptApiSolrRepository.findUsingEdismax(any(), any(), any(), any(), isNull()))
            .thenReturn(Page.empty())

        val cards = mutableSetOf(CustomerCard(cardNumber = "0012"), CustomerCard("0013"))

        receiptApiDao.getByLoyaltyCardNumber(
            cards = cards, dateFrom = null, dateTo = null, storeId = null, totalFrom = null, totalTo = null,
            sortField = null, direction = null, pageNo = "0", pageSize = "10"
        )

        verify(receiptApiSolrRepository).findUsingEdismax(
            fieldQueryCaptor.capture(), any(), any(), any(), isNull()
        )

        assertTrue(fieldQueryCaptor.value.contains("loyaltyCardNumber:(0012 OR 0013)"))
    }


    @Test
    fun `getByLoyaltyCardNumber builds correct query with null dates and totals`() {
        val fieldQueryCaptor = ArgumentCaptor.forClass(List::class.java as Class<List<String>>)
        `when`(receiptApiSolrRepository.findUsingEdismax(any(), any(), any(), any(), isNull()))
            .thenReturn(Page.empty())

        val cards = mutableSetOf(CustomerCard(cardNumber = "0012"))

        receiptApiDao.getByLoyaltyCardNumber(
            cards = cards, dateFrom = null, dateTo = null, storeId = "188", totalFrom = null, totalTo = null,
            sortField = null, direction = null, pageNo = "0", pageSize = "10"
        )

        verify(receiptApiSolrRepository).findUsingEdismax(
            fieldQueryCaptor.capture(), any(), any(), any(), isNull()
        )

        val capturedQuery = fieldQueryCaptor.value
        assertTrue(capturedQuery.contains("loyaltyCardNumber:0012"))
        assertTrue(capturedQuery.contains("storeId:188"))
        assertTrue(capturedQuery.contains("totalAmount:[0 TO ${Int.MAX_VALUE}]"))

        assertTrue(capturedQuery.none { it.startsWith("totalDateTime:") })
    }
}