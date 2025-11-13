package pl.exaco.receiptApi.configuration.properties

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpringProperties(
    val datasource: DatasourceProperties,
    @JsonProperty("alloy-db")
    val alloyDb: DatasourceProperties,
    val cloud: CloudProperties,
)
