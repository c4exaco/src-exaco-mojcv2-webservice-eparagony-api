package pl.exaco.receiptApi.configuration.properties

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CloudProperties(
    val liquibaseBucketName: String? = null,
)