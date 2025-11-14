package pl.exaco.receiptApi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*
import pl.exaco.receiptApi.model.dto.ReceiptApiBasicModel
import pl.exaco.receiptApi.model.dto.ReceiptApiModel
import pl.exaco.receiptApi.model.dto.ReceiptBasicStore
import pl.exaco.receiptApi.model.dto.SortField
import pl.exaco.receiptApi.service.ReceiptApiService

@RestController
@RequestMapping("/ReceiptService/v1/ReceiptController", produces = ["application/json"])
@SecurityRequirement(name = "bearerAuth")
class ReceiptApiController(private val receiptApiService: ReceiptApiService) {

    @GetMapping("/List")
    @Operation(summary = "Lista paragonów dla aplikacji",
            description = "Endpoint do pobrania listy porstych modeli paragonu w aplikacji w dziale e-paragony")
    @Throws(Exception::class)
    @ApiResponses(
            ApiResponse(responseCode = "200", description = "CREATED"),
            ApiResponse(responseCode = "500", description = "SERVICE_ERROR"),
            ApiResponse(responseCode = "401", description = "AUTH_ERROR")
    )
    fun getCustomerReceiptList(@RequestHeader("Auth-Token") authToken: String?,
                               @RequestHeader("CustomerId", required = true) customerId: String,
                               @RequestParam loyaltyCardNumber: String,
                               @RequestParam(required = false) articleName: String?,
                               @RequestParam(required = false, name = "dateFrom") dateFrom: String?,
                               @RequestParam(required = false, name = "dateTo") dateTo: String?,
                               @RequestParam(required = false, name = "storeId") storeId: String?,
                               @RequestParam(required = false, name = "totalFrom") totalFrom: String?,
                               @RequestParam(required = false, name = "totalTo") totalTo: String?,
                               @RequestParam(required = false, name = "pageNumber") pageNo: String?,
                               @RequestParam(required = false, name = "pageSize") pageSize: String?,
                               @RequestParam(required = false, name = "sortField") sortField: SortField?,
                               @RequestParam(required = false, name = "sortDirection") direction: Sort.Direction?,
                               httpServletResponse: HttpServletResponse
    ): Page<ReceiptApiBasicModel> {
        val response = receiptApiService.getCustomerReceiptList(customerId.toInt(),
            articleName,
            dateFrom,
            dateTo,
            storeId,
            totalFrom?.toBigDecimal(),
            totalTo?.toBigDecimal(),
            sortField,
            direction,
            pageNo,
            pageSize,loyaltyCardNumber)
        httpServletResponse.addHeader("c-total-d", response.second)
        return response.first
    }

    @GetMapping
    @Operation(summary = "Detale paragonu",
            description = "Endpoint do pobrania detali paragonu w aplikacji w dziale e-paragony")
    @Throws(Exception::class)
    @ApiResponses(
            ApiResponse(responseCode = "200", description = "CREATED"),
            ApiResponse(responseCode = "500", description = "SERVICE_ERROR"),
            ApiResponse(responseCode = "401", description = "AUTH_ERROR")
    )
    fun getByBarcode(@RequestHeader("Auth-Token") authToken: String?,
                     @RequestHeader("CustomerId", required = true) customerId: String,
                     @RequestParam receiptBarcode: String): ReceiptApiModel? {
        return receiptApiService.getByBarcode(receiptBarcode, customerId)
    }

    @GetMapping("/Shop/List")
    @Operation(summary = "Lista sklepów w konkeście paragonów dla klienta",
            description = "Endpoint do pobrania listy sklepów do wyszukiwarki e-paragonów dla klienta")
    @Throws(Exception::class)
    @ApiResponses(
            ApiResponse(responseCode = "200", description = "CREATED"),
            ApiResponse(responseCode = "500", description = "SERVICE_ERROR"),
            ApiResponse(responseCode = "401", description = "AUTH_ERROR")
    )
    fun getShopList(@RequestHeader("Auth-Token") authToken: String?,
                    @RequestHeader("CustomerId", required = false) customerId: String?,
                    @RequestParam loyaltyCardNumber: String): Set<ReceiptBasicStore> {
        return receiptApiService.getShopList(loyaltyCardNumber)
    }
}