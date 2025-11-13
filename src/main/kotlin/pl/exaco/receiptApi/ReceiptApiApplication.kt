package pl.exaco.receiptApi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.context.config.annotation.RefreshScope
import pl.exaco.receiptApi.const.vmParam
import java.lang.management.ManagementFactory

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
	runApplication<ReceiptApiApplication>(*args)
	val activeProfile = ManagementFactory.getRuntimeMXBean().inputArguments
			.find { it.contains(vmParam) }?.substringAfter(vmParam) ?: "DEFAULT"
	println("============================== RECEIPT SERVER STARTED! CONFIG: ${activeProfile.uppercase()}. ==============================")
}
