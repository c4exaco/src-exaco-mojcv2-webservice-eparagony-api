package pl.exaco.receiptApi.connector.upos

import org.springframework.stereotype.Service
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import pl.exaco.receiptApi.configuration.AppConfig
import pl.exaco.receiptApi.exception.UposException
import pl.exaco.receiptApi.logger.Logger.Companion.logger
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

@Service
class SoapRequestExecutor(val appConfig: AppConfig): ISoapRequestExecutor {
    override fun executeSOAPRequest(soapXML: String, customerId: Int): Document? {
        val startNanos = System.nanoTime()
        val path: String = appConfig.exorigo?.exorigoWebservicePath!!
        val socket = Socket()
        val output: StringBuilder = java.lang.StringBuilder()
        try {
            socket.connect(InetSocketAddress(appConfig.exorigo?.exorigoWebserviceIp!!, appConfig.exorigo?.exorigoWebservicePort?.toInt()!!), 4000)
            socket.soTimeout = 10000
            val stream = socket.getOutputStream()
            val writer = OutputStreamWriter(stream, "UTF-8")
            val bufferedWriter = BufferedWriter(writer)
            val soapXMLData = soapXML.toByteArray(charset(writer.encoding))
            bufferedWriter.write("POST $path HTTP/1.0\r\n")
            bufferedWriter.write(
                """
                Content-Length: ${soapXMLData.size}
                
                """.trimIndent()
            )
            bufferedWriter.write("Content-Type: text/xml; charset=\"utf-8\"\r\n")
            bufferedWriter.write("\r\n")
            bufferedWriter.flush()
            stream.write(soapXMLData)
            val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                output.append(line)
            }
            return getDocument(getSOAPBody(output.toString()))
        } catch (ex: Exception) {
            throw UposException("Soap request fail")
        } finally {
            val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos)
            logger.info("Upos soap execute. Request: ${soapXML.replace("\n","").replace("  ","")}  Response: $output in $duration. CustomerId: $customerId.")
            socket.close()
        }
    }

    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    fun getDocument(output: String?): Document? {
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val src = InputSource()
        src.characterStream = StringReader(output!!)
        return builder.parse(src)
    }

    fun getSOAPBody(output: String?): String? {
        var outp = output
        val pattern = Pattern.compile("<soap:Body>(.+?)</soap:Body>")
        val matcher = outp?.let { pattern.matcher(it) }
        matcher?.find()
        outp = matcher?.group(1)
        return outp
    }
}