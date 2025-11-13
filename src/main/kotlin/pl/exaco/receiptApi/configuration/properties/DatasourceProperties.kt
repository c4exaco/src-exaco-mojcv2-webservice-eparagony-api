package pl.exaco.receiptApi.configuration.properties

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DatasourceProperties(
    var url: String,
    var username: String,
    var password: String,
    val driverClassName: String,
    var name: String
)