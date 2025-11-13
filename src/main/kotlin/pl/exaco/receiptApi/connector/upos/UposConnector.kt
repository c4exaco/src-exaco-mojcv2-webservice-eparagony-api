package pl.exaco.receiptApi.connector.upos

import org.springframework.stereotype.Service
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import pl.exaco.receiptApi.configuration.AppConfig
import pl.exaco.receiptApi.exception.UposCustomerAccountDetailsException
import pl.exaco.receiptApi.exception.UposGetCardException
import pl.exaco.receiptApi.logger.Logger.Companion.logger
import pl.exaco.receiptApi.model.auth.CustomerCard
import java.text.ParseException
import java.text.SimpleDateFormat

@Service
class UposConnector(
    val appConfig: AppConfig,
    private val iSoapRequestExecutor: ISoapRequestExecutor
) {

    @Throws(Exception::class)
    fun getCards(customerId: Int): Set<CustomerCard> {
        val document = iSoapRequestExecutor.executeSOAPRequest(prepareGetCustomerCards(customerId), customerId)
        val resultCode: String? = getDocumentValue(document, "ResultCode")
        if (resultCode != "Success") {
            throw UposGetCardException("Błąd pobierania kart")
        }
        val cardNumberWithStatusList: MutableSet<CustomerCard> = mutableSetOf()
        getDocumentValue(document, "CardNumberWithStatusList")
        for (cardNumberWithStatus in document?.getElementsByTagName("CardNumberWithStatus")?.let { asList(it) }!!) {
            val cardNumberWithStatusElement = cardNumberWithStatus as Element
            val cardNumber = cardNumberWithStatusElement.getElementsByTagName("CardNumber").item(0).textContent
            val status = cardNumberWithStatusElement.getElementsByTagName("Status").item(0).textContent
            cardNumberWithStatusList.add(
                CustomerCard(
                    cardNumber = cardNumber,
                    status = CardStatus.valueOf(status.uppercase()).ordinal.toShort()
                )
            )
        }
        logger.info("Get cards success. CustomerId: $customerId. Cards: ${cardNumberWithStatusList.joinToString(", ") { it.cardNumber }}")
        return cardNumberWithStatusList
    }

    private fun prepareGetCustomerCards(customerId: Int): String {
        return """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:mob="http://www.exorigo-upos.pl/upos/loyalty/mobileappwebservice">
               <soapenv:Header/>
               <soapenv:Body>
                  <mob:GetCustomerCards>
                     <!--Optional:-->
                     <mob:request>
                        <mob:RequestId>1</mob:RequestId>
                        <mob:CustomerId>$customerId</mob:CustomerId>
                     </mob:request>
                  </mob:GetCustomerCards>
               </soapenv:Body>
            </soapenv:Envelope>"""
    }

    @Throws(Exception::class)
    fun getCustomerAccountDetails(cardNumber: String, customerId: Int): CustomerAccountDetails {
        val document: Document? =
            iSoapRequestExecutor.executeSOAPRequest(prepareGetCustomerAccountDetails(cardNumber), customerId)
        val resultCode: String? = getDocumentValue(document, "ResultCode")
        if (resultCode != "Success") {
            throw UposCustomerAccountDetailsException("Błąd pobierania danych użytkownika")
        }
        val customerIdResponse = getDocumentValue(document, "CustomerId")?.toInt()
        val accountId = getDocumentValue(document, "AccountId")?.toInt()
        val accountBalance = getDocumentValue(document, "Points")?.toLong()
        val discountAmount = getDocumentValue(document, "DiscountAmount")?.toLong()
        val accountStatus = getDocumentValue(document, "AccountStatus")?.toInt()
        val statusUpdateDateTimeString = getDocumentValue(document, "StatusUpdateDateTime")
        val statusUpdateDateTime: Long =
            if (statusUpdateDateTimeString != null && statusUpdateDateTimeString.isNotEmpty()) {
                stringDateToMilliSec(statusUpdateDateTimeString, "yyyy-MM-dd'T'HH:mm:ss")
            } else {
                0L
            }
        val details = CustomerAccountDetails(
            customerIdResponse,
            accountId,
            accountBalance,
            discountAmount,
            accountStatus,
            statusUpdateDateTime
        )
        logger.info("Get customer details success. CustomerId: $customerId. $details")
        return details
    }

    @Throws(ParseException::class)
    fun stringDateToMilliSec(stringDate: String?, format: String): Long {
        val formatter = SimpleDateFormat(format)
        val date = formatter.parse(stringDate)
        return date.time
    }

    private fun prepareGetCustomerAccountDetails(cardNumber: String): String {
        return """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:mob="http://www.exorigo-upos.pl/upos/loyalty/mobileappwebservice">
               <soapenv:Header/>
               <soapenv:Body>
                  <mob:GetCustomerAccountDetails>
                     <mob:request>
                        <mob:RequestId>10</mob:RequestId>
                        <mob:CardNumber>$cardNumber</mob:CardNumber>
                     </mob:request>
                  </mob:GetCustomerAccountDetails>
               </soapenv:Body>
            </soapenv:Envelope>"""
    }

    private fun getDocumentValue(document: Document?, valueTag: String): String? {
        return if (document?.getElementsByTagName(valueTag)?.item(0) != null) {
            document.getElementsByTagName(valueTag).item(0).textContent
        } else {
            if ("ResultCode" == valueTag) {
                "UnknownFail"
            } else {
                "0"
            }
        }
    }

    private fun asList(n: NodeList): List<Node?> {
        return if (n.length == 0) emptyList<Node>() else NodeListWrapper(n)
    }

}