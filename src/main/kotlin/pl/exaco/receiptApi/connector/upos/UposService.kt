package pl.exaco.receiptApi.connector.upos

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import pl.exaco.receiptApi.model.auth.CustomerCard
import java.util.*

@Service
open class UposService(private val uposConnector: UposConnector) {
    @Throws(Exception::class)
    @Cacheable(value = ["cardsCache"], key = "#customerId")
    open fun getActualCustomerCards(customerId: Int): Set<CustomerCard> {
        return uposConnector.getCards(customerId)
    }

    @Throws(Exception::class)
    @Cacheable(value = ["detailsCache"], key = "{#cardNumber, #customerId}")
    open fun getCustomerAccountDetails(cardNumber: String, customerId: Int): CustomerAccountDetails? {
        return uposConnector.getCustomerAccountDetails(cardNumber, customerId)
    }
}

open class NodeListWrapper(private val list: NodeList) : AbstractList<Node>(),
    RandomAccess {
    override fun get(index: Int): Node {
        return list.item(index)
    }

    override val size: Int
        get() = list.length
}

enum class CardStatus(val cardStatusId: Int, val cardStatusName: String) {
    ACTIVE(0, "Aktywna"),
    DEACTIVATED(3, "Dezaktywowana"),
    NOTUSED(10, "Nieuzywana"),
    NOTACTIVE(20, "Nieaktywna"),
    AFTERFUSION(30, "Po fuzji"),
}

data class CustomerAccountDetails(
    val customerId: Int? = null,
    val accountId: Int? = null,
    val accountBalance: Long? = null,
    val discountAmount: Long? = null,
    val accountStatus: Int? = null,
    val statusUpdateDateTime: Long? = null
)