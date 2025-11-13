package pl.exaco.receiptApi.connector.upos

import org.w3c.dom.Document

interface ISoapRequestExecutor {
    fun executeSOAPRequest(soapXML: String, customerId: Int): Document?
}