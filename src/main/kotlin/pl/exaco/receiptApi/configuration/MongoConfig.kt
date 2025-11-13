package pl.exaco.receiptApi.configuration

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(
    basePackages = ["pl.exaco.receiptApi.mongorepository"],
    mongoTemplateRef = "mongoTemplate"
)
class MongoConfig(
    private val appConfig: AppConfig
) {
    @Bean
    fun mongoClient(): MongoClient {
        val mongoProps = appConfig.eparagony?.api?.mongodb
            ?: throw IllegalStateException("Brak konfiguracji 'mongodb' w AppConfig")

        val connectionString = "mongodb://${mongoProps.username}:${mongoProps.password}" +
                "@${mongoProps.host}:${mongoProps.port}/${mongoProps.database}" +
                "?authSource=${mongoProps.authenticationDatabase}"

        val mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(connectionString))
            .build()

        println("✅ Ręczna konfiguracja klienta MongoDB... Połączono.")
        return MongoClients.create(mongoClientSettings)
    }

    @Bean
    fun mongoDatabaseFactory(mongoClient: MongoClient): MongoDatabaseFactory {
        val dbName = appConfig.eparagony?.api?.mongodb?.database
            ?: throw IllegalStateException("Brak nazwy bazy danych w AppConfig")
            
        return SimpleMongoClientDatabaseFactory(mongoClient, dbName)
    }

    @Bean("mongoTemplate")
    fun mongoTemplate(mongoDatabaseFactory: MongoDatabaseFactory): MongoTemplate {
        return MongoTemplate(mongoDatabaseFactory)
    }
}