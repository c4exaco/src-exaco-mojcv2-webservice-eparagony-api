package pl.exaco.receiptApi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.context.config.annotation.RefreshScope
import java.io.File

@SpringBootApplication(
	exclude = [
		MongoAutoConfiguration::class,
		MongoDataAutoConfiguration::class
	]
)
@EnableCaching
@RefreshScope
class ReceiptApiApplication

fun main(args: Array<String>) {
	val profilesFromProps = System.getProperty("spring.profiles.active") ?: ""
	val profilesFromArgs = args.firstOrNull { it.startsWith("--spring.profiles.active=") } ?: ""
	val isLocalProfileSetManually = profilesFromProps.contains("local", ignoreCase = true) ||
			profilesFromArgs.contains("local", ignoreCase = true)
	val builder = SpringApplicationBuilder(ReceiptApiApplication::class.java)
	if (isLocalProfileSetManually) {
		println("✅ 'local' profile detected from startup arguments. Skipping automatic profile detection.")
	} else {
		val credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
		if (!credentialsPath.isNullOrBlank()) {
			val credentialsFile = File(credentialsPath)
			if (credentialsFile.exists()) {
				val objectMapper = ObjectMapper()
				val credentialsJson: Map<String, Any> = objectMapper.readValue(credentialsFile)
				val projectId = credentialsJson["project_id"] as? String
				if (projectId?.contains("labo", ignoreCase = true) == true) {
					println("✅ GOOGLE_APPLICATION_CREDENTIALS contains 'labo'. Activating 'labo' profile.")
					builder.profiles("labo")
				} else if (projectId?.contains("prod", ignoreCase = true) == true) {
					println("✅ GOOGLE_APPLICATION_CREDENTIALS contains 'prod'. Activating 'prod' profile.")
					builder.profiles("prod")
				}
			} else {
				println("⚠️ Credentials file specified in GOOGLE_APPLICATION_CREDENTIALS not found at: $credentialsPath")
			}
		}
	}
	builder.build().run(*args)
}
