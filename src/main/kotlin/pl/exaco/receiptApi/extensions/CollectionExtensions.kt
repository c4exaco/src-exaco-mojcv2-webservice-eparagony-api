package pl.exaco.receiptApi.extensions

import pl.exaco.receiptApi.helpers.ReceiptApiHelper.Companion.getSolrArticleName

fun MutableList<String>.swap(articleName: String?): List<String> {
    if (articleName.isNullOrEmpty()) {
        return this
    }
    val returnList = mutableListOf<String>()
    repeat(this.size) {
        var firstFoundArticleIndex = -1
        val searchList = articleName.trim().split(" ")
        searchList.forEach { word ->
            if (firstFoundArticleIndex == -1) {
                firstFoundArticleIndex = this.indexOfFirst { getSolrArticleName(it).lowercase().contains(word.lowercase()) }
            }
        }
        if (firstFoundArticleIndex != -1) {
            val article = this[firstFoundArticleIndex]
            this.removeAt(firstFoundArticleIndex)
            returnList.add(article)
        }
    }
    return returnList.plus(this)
}