package pl.exaco.receiptApi.configuration

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.stereotype.Component
import pl.exaco.receiptApi.configuration.properties.EforceProperties
import pl.exaco.receiptApi.configuration.properties.EparagonyProperties
import pl.exaco.receiptApi.configuration.properties.ExorigoProperties
import pl.exaco.receiptApi.configuration.properties.SpringProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class AppConfig {
    val exorigo: ExorigoProperties? = null
    val eforce: EforceProperties? = null
    val eparagony: EparagonyProperties? = null
    val spring: SpringProperties? = null
}