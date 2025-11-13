package pl.exaco.receiptApi.mongorepository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import pl.exaco.receiptApi.model.IntranetStore
import pl.exaco.receiptApi.model.external.EForceArticle
import pl.exaco.receiptApi.model.transaction.TransactionForMobile

@Repository
interface ReceiptApiIntranetStoresRepository : MongoRepository<IntranetStore, String>

@Repository
interface ReceiptApiEForceArticleRepository : MongoRepository<EForceArticle, String> {
    fun findByArticleName(articleName: String): EForceArticle?
    fun findByBarcode(barcode: String): EForceArticle?
}

@Repository
interface ReceiptApiTransactionRepository : MongoRepository<TransactionForMobile, String> {
    fun findByBarcode(barcode: String): TransactionForMobile?
    fun findByBarcodeIn(barcodes: List<String>) : List<TransactionForMobile>?
}