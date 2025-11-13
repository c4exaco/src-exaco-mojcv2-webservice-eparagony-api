package pl.exaco.receiptApi.solrrepository

import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.util.ClientUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import pl.exaco.receiptApi.model.dto.ReceiptSolrModel
import java.util.*

@Repository
class ReceiptApiSolrRepository(
    private val solrClient: SolrClient
) {
    private val SOLR_CORE_NAME = "receipt"

    fun findById(id: String): Optional<ReceiptSolrModel> {
        val queryValue = "id:\"${ClientUtils.escapeQueryChars(id)}\""
        val query = SolrQuery(queryValue)
        query.rows = 1

        try {
            val response = solrClient.query(SOLR_CORE_NAME, query)
            val results = response.getBeans(ReceiptSolrModel::class.java)
            return Optional.ofNullable(results.firstOrNull())
        } catch (e: Exception) {
            println("Błąd podczas zapytania Solr (findById: $id): ${e.message}")
            return Optional.empty()
        }
    }

    fun findByLoyaltyCardNumber(loyaltyCardNumber: String): List<ReceiptSolrModel> {
        val queryValue = "loyaltyCardNumber:\"${ClientUtils.escapeQueryChars(loyaltyCardNumber)}\""
        val query = SolrQuery(queryValue)
        query.rows = 100

        try {
            val response = solrClient.query(SOLR_CORE_NAME, query)
            return response.getBeans(ReceiptSolrModel::class.java)
        } catch (e: Exception) {
            println("Błąd podczas zapytania Solr (findByLoyaltyCardNumber): ${e.message}")
            return emptyList()
        }
    }

    fun findUsingEdismax(
        fieldQuery: List<String>,
        queryField: String,
        pageable: Pageable,
        minimumShouldMatch: String? = null,
        boostQuery: String? = null
    ): Page<ReceiptSolrModel> {

        val query = SolrQuery()
        query.set("defType", "edismax")
        query.set("q", fieldQuery.joinToString(" AND "))
        query.set("qf", queryField)

        if (minimumShouldMatch != null) {
            query.set("mm", minimumShouldMatch)
        }
        if (boostQuery != null) {
            query.add("bq", boostQuery)
        }

        query.start = pageable.offset.toInt()
        query.rows = pageable.pageSize
        pageable.sort.forEach { order ->
            val sortDirection = if (order.isAscending) SolrQuery.ORDER.asc else SolrQuery.ORDER.desc
            query.addSort(order.property, sortDirection)
        }

        try {
            val response = solrClient.query(SOLR_CORE_NAME, query)
            val results = response.getBeans(ReceiptSolrModel::class.java)
            val totalCount = response.results.numFound

            return PageImpl(results, pageable, totalCount)

        } catch (e: Exception) {
            println("Błąd podczas zapytania Edismax Solr: ${e.message}")
            return PageImpl(emptyList(), pageable, 0)
        }
    }
}