package pl.exaco.receiptApi.configuration.properties

import com.fasterxml.jackson.annotation.JsonProperty

class EparagonyProperties(
    val api: Api
)

data class Api(
    val mongodb: MongoConfig,
    val solr: SolrConfig,
    val indexerPoolSize: Int
)

data class MongoConfig(
    @param:JsonProperty("authentication-database")
    val authenticationDatabase: String,

    val database: String,
    val host: String,
    val password: String,
    val port: Int,
    val username: String
)

data class SolrConfig(
    val host: String,
    val repositories: SolrRepositories
)

data class SolrRepositories(
    val enabled: Boolean
)