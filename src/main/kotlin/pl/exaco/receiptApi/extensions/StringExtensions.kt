package pl.exaco.receiptApi.extensions

fun String.substringBetween(delimiter: String): String {
    if (this.substringAfterLast(';') == "") {
        return this.dropLast(1).substringAfterLast(delimiter)
    }
    return this.replace(delimiter.plus(this.substringAfterLast(delimiter)), "").substringAfterLast(delimiter)
}

fun String.substringBeforeInc(delimiter: String): String {
    return this.substringBefore(delimiter).plus(delimiter)
}

fun String.replaceIfStartsWith(sequence: String): String {
    return if (this.startsWith(sequence)) this.replaceFirst(sequence,"") else this
}