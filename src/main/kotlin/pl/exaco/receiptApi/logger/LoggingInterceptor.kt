package pl.exaco.receiptApi.logger

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import pl.exaco.receiptApi.logger.Logger.Companion.logger
import java.io.IOException
import java.nio.charset.StandardCharsets

class LoggingInterceptor : ClientHttpRequestInterceptor {
    @Throws(IOException::class)
    override fun intercept(
        req: HttpRequest, reqBody: ByteArray, ex: ClientHttpRequestExecution
    ): ClientHttpResponse {
        logger.info("Request uri: {}", req.uri)
        logger.info("Request body: {}", String(reqBody, StandardCharsets.UTF_8))
        val response = ex.execute(req, reqBody)
        logger.info("Response statusCode: {}", response.statusCode)
        return response
    }
}