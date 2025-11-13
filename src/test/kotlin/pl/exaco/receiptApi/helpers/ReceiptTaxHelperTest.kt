package pl.exaco.receiptApi.helpers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pl.exaco.receiptApi.model.transaction.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime


internal class ReceiptTaxHelperTest {

    private lateinit var receiptTaxHelper: ReceiptTaxHelper

    @BeforeEach
    fun initializeEach() {
        receiptTaxHelper = ReceiptTaxHelper()
    }

    @Test
    fun `prepareTax test with 3 types of tax and one type twice, sum and display success`() {
        val mobileItem = MobileItem(
                null, null, null, null, "10.00", "20", null, null, "0.5", null,
                MobileItemTax("23.00", "A"), null, null, null, null, null, null, "2.30"
        )
        val mobileItem2 = MobileItem(
                null, null, null, null, "100.00", "200", null, null, "0.5", null,
                MobileItemTax("12.00", "B"), null, null, null, null, null, null, "12.00"
        )
        val mobileItem3 = MobileItem(
                null, null, null, null, "50.00", "100", null, null, "0.5", null,
                MobileItemTax("5.00", "C"), null, null, null, null, null, null, "2.50"
        )
        val mobileItem4 = MobileItem(
                null, null, null, null, "50.00", "100", null, null, "0.5", null,
                MobileItemTax("5.00", "C"), null, null, null, null, null, null, "2.50"
        )
        val mongoData = TransactionForMobile("1", "188", 1, 1, PosType.HECTRONIC, null, null, null, "210.00",
                listOf(mobileItem, mobileItem2, mobileItem3, mobileItem4), null)
        val tax = receiptTaxHelper.prepareTax(mongoData)

        val itemsWithNoPawn = mongoData.items?.filterNot { it.articleType?.equals(ArticleType.PAWN) ?: false }

        val sumOfTax = receiptTaxHelper.indicateTaxElements(itemsWithNoPawn)

        val nonTaxTotal = mongoData.totalAmount?.toBigDecimal()
                ?.minus(sumOfTax.keys.first())
        assertEquals(mongoData.totalAmount?.toBigDecimal(), tax?.taxPTU?.plus(nonTaxTotal!!))
        assertEquals("17.34".toBigDecimal(), tax?.taxPTU)
        assertEquals("192.66".toBigDecimal(), nonTaxTotal)
        assertEquals("1.87".toBigDecimal(), tax?.taxAmounts?.get("Kwota A 23.00%"))
        assertEquals("4.76".toBigDecimal(), tax?.taxAmounts?.get("Kwota C 5.00%"))
    }

    @Test
    fun `prepareTax test with 3 types of tax and one type twice and one Pawn, sum and display success`() {
        val mobileItem = MobileItem(
                null, null, null, null, "10.00", "20", null, null, "0.5", null,
                MobileItemTax("23.00", "A"), null, null, null, null, null, null, "2.30"
        )
        val mobileItem2 = MobileItem(
                null, null, null, null, "100.00", "200", null, null, "0.5", null,
                MobileItemTax("12.00", "B"), null, null, null, null, null, null, "12.00"
        )
        val mobileItem3 = MobileItem(
                null, null, null, null, "50.00", "100", null, null, "0.5", null,
                MobileItemTax("5.00", "C"), null, null, null, null, null, null, "2.50"
        )
        val mobileItem4 = MobileItem(
                null, null, null, null, "50.00", "100", null, null, "0.5", null,
                MobileItemTax("5.00", "C"), null, null, null, null, null, null, "2.50"
        )
        val mobileItem5Pawn = MobileItem(
                null, null, null, null, "50.00", "100", null, "0.5", "0.5", null,
                MobileItemTax("5.00", "E"), null, null, null, null, null, null, "2.50",
                articleType = ArticleType.PAWN
        )
        val mongoData = TransactionForMobile("1", "188", 1, 1, PosType.HECTRONIC, null, null, null, "210.00",
                listOf(mobileItem, mobileItem2, mobileItem3, mobileItem4, mobileItem5Pawn), null)
        val tax = receiptTaxHelper.prepareTax(mongoData)
        val nonTaxTotal = mongoData.totalAmount?.toBigDecimal()
                ?.minus(tax?.taxPTU ?: BigDecimal.ZERO)
        assertEquals(mongoData.totalAmount?.toBigDecimal(), nonTaxTotal?.plus(tax?.taxPTU!!))
        assertEquals("17.34".toBigDecimal(), tax?.taxPTU)
        assertEquals("192.66".toBigDecimal(), mongoData.totalAmount?.toBigDecimal()?.minus(tax?.taxPTU!!))
        assertEquals("1.87".toBigDecimal(), tax?.taxAmounts?.get("Kwota A 23.00%"))
        assertEquals("4.76".toBigDecimal(), tax?.taxAmounts?.get("Kwota C 5.00%"))
    }

    @Test
    fun `sortTaxTypeAmounts test sort indicated tax amounts success`() {
        var taxTypeAmounts = mutableMapOf<String, BigDecimal>()
        taxTypeAmounts["CA;Sprzed. opod. PTU C"] = "0.92".toBigDecimal()
        taxTypeAmounts["CB;Kwota C 5.00%"] = "0.92".toBigDecimal()
        taxTypeAmounts["BA;Sprzed. opod. PTU B"] = "0.92".toBigDecimal()
        taxTypeAmounts["BB;Kwota B 12.00%"] = "0.92".toBigDecimal()
        taxTypeAmounts["AA;Sprzed. opod. PTU A"] = "0.46".toBigDecimal()
        taxTypeAmounts["AB;Kwota A 23.00%"] = "0.46".toBigDecimal()

        taxTypeAmounts = receiptTaxHelper.sortTaxTypeAmounts(taxTypeAmounts).toMutableMap()
        println(taxTypeAmounts)
        assertTrue(taxTypeAmounts.toString().startsWith("{Sprzed. opod. PTU A"))
        assertTrue(taxTypeAmounts.toString().substringBeforeLast("}").substringAfterLast(",") == " Kwota C 5.00%=0.92")
    }

    @Test
    fun `prepare pawn amount when one pawn`() {
        val mobileItem = MobileItem(
                null, null, null, null, "10.00", "1", null, null, null, null,
                MobileItemTax("23.00", "A"), null, null, null, null, null, null, "2.30"
        )
        val mobileItem2 = MobileItem(
                null, null, null, null, "100.00", "1", null, null, null, null,
                MobileItemTax("12.00", "B"), null, null, null, null, null, null, "12.00"
        )
        val mobileItem3 = MobileItem(
                null, null, null, null, "50.00", "1", null, null, null, null,
                MobileItemTax("5.00", "C"), null, null, null, null, null, null, "2.50"
        )
        val mobileItem4 = MobileItem(
                null, null, null, null, "50.00", "100", null, "0.5", null, null,
                MobileItemTax("5.00", "C"), null, null, null, null, null, null, "2.50",
                articleType = ArticleType.PAWN
        )
        val items = listOf(mobileItem, mobileItem2, mobileItem3, mobileItem4)
        val amount = receiptTaxHelper.preparePawnAmount(items)
        assertTrue(amount == BigDecimal.valueOf(50.0))
    }

    @Test
    fun `prepare pawn amount when two pawn`() {
        val mobileItem = MobileItem(
                null, null, null, null, "10.00", "1", null, null, null, null,
                MobileItemTax("23.00", "A"), null, null, null, null, null, null, "2.30"
        )
        val mobileItem2 = MobileItem(
                null, null, null, null, "100.00", "1", null, "100.0", null, null,
                MobileItemTax("12.00", "E"), null, null, null, null, null, null, "12.00",
                articleType = ArticleType.PAWN
        )
        val mobileItem3 = MobileItem(
                null, null, null, null, "50.00", "1", null, null, null, null,
                MobileItemTax("5.00", "C"), null, null, null, null, null, null, "2.50"
        )
        val mobileItem4 = MobileItem(
                null, null, null, null, "50.00", "100", null, "0.5", null, null,
                MobileItemTax("5.00", "E"), null, null, null, null, null, null, "2.50",
                articleType = ArticleType.PAWN
        )
        val items = listOf(mobileItem, mobileItem2, mobileItem3, mobileItem4)
        val amount = receiptTaxHelper.preparePawnAmount(items)
        assertTrue(amount == BigDecimal.valueOf(150.0))
    }

    @Test
    fun `prepare pawn amount when no pawn`() {
        val mobileItem = MobileItem(
                null, null, null, null, "10.00", "1", null, null, null, null,
                MobileItemTax("23.00", "A"), null, null, null, null, null, null, "2.30"
        )
        val mobileItem2 = MobileItem(
                null, null, null, null, "100.00", "1", null, "100.0", null, null,
                MobileItemTax("12.00", "E"), null, null, null, null, null, null, "12.00"
        )
        val mobileItem3 = MobileItem(
                null, null, null, null, "50.00", "1", null, null, null, null,
                MobileItemTax("5.00", "C"), null, null, null, null, null, null, "2.50"
        )
        val mobileItem4 = MobileItem(
                null, null, null, null, "50.00", "100", null, "0.5", null, null,
                MobileItemTax("5.00", "E"), null, null, null, null, null, null, "2.50"
        )
        val items = listOf(mobileItem, mobileItem2, mobileItem3, mobileItem4)
        val amount = receiptTaxHelper.preparePawnAmount(items)
        assertNull(amount)
    }

    @Test
    fun `prepare vat from json`() {
        val fileContent = this::class.java.classLoader.getResource("vatTaxTestReceipt.json")?.readText()
        val mapped = Gson().fromJson<Set<MobileItem>>(fileContent, object: TypeToken<Set<MobileItem>>() {}.type)
        val mongoData = TransactionForMobile("1", "188", 1, 1, PosType.HECTRONIC,
            LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "609.36", mapped.toList(), null)

        val tax = receiptTaxHelper.prepareTax(mongoData)
        val nonTaxTotal = mongoData.totalAmount?.toBigDecimal()
            ?.minus(tax?.taxPTU ?: BigDecimal.ZERO)
        assertEquals(mongoData.totalAmount?.toBigDecimal(), nonTaxTotal?.plus(tax?.taxPTU!!)?.setScale(2, RoundingMode.HALF_UP))
        assertEquals("45.44".toBigDecimal(), tax?.taxPTU)
        assertEquals("563.92".toBigDecimal(), mongoData.totalAmount?.toBigDecimal()?.minus(tax?.taxPTU!!))
        assertEquals("0.74".toBigDecimal(), tax?.taxAmounts?.get("Kwota A 23.00%"))
        assertEquals("44.43".toBigDecimal(), tax?.taxAmounts?.get("Kwota B 8.00%"))
        assertEquals("0.27".toBigDecimal(), tax?.taxAmounts?.get("Kwota C 5.00%"))

    }

}