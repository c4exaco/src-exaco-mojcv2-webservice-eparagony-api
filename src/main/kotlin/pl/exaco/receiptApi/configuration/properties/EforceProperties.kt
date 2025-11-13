package pl.exaco.receiptApi.configuration.properties

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class EforceProperties(
    val eforceUrlMultiArticle: String,
    val eforceUrlSingleArticle: String,
    val eforcePassword: String,
    val eForceImageUrl: String,
    val eForceCategoryUrl: String,
    val eforceUsername: String,
    val eforcePoolSize: Int,
)