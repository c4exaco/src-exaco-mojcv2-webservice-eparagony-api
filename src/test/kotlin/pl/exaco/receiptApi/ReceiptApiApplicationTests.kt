//package pl.exaco.receiptApi
//
//import org.junit.jupiter.api.Assertions
//import org.junit.jupiter.api.Disabled
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.context.ActiveProfiles
//import pl.exaco.receiptApi.connector.eforce.EForceConnector
//import pl.exaco.receiptApi.connector.upos.UposConnector
//import pl.exaco.receiptApi.model.auth.CustomerCard
//
//@SpringBootTest
//@ActiveProfiles("locallabo")
//class ReceiptApiApplicationTests {
//
//    @Autowired
//    lateinit var eForceConnector: EForceConnector
//
//    @Autowired
//    lateinit var uposConnector: UposConnector
//
//
//    @Test
//    fun contextLoads() {
//        val cards = uposConnector.getCards(87070)
//        Assertions.assertTrue(cards.isNotEmpty())
//    }
//
//    @Test
//    @Disabled
//    fun `eForceConnection batch false fetch success`() {
//        val product = eForceConnector.getEForceArticles(setOf("10"), setOf(CustomerCard(cardNumber = "34543")))
//        Assertions.assertTrue(product.keys.contains("10"))
//    }
//
//    @Test
//    @Disabled
//    fun `eForceConnection batch true fetch fail because of Eforce main ean return bug`() {
//        val product = eForceConnector.getEForceArticles(setOf("10"), setOf(CustomerCard(cardNumber = "243423")))
//        Assertions.assertFalse(product.keys.contains("10"))
//        Assertions.assertTrue(product.keys.contains("2000000534213"))
//    }
//
//}
