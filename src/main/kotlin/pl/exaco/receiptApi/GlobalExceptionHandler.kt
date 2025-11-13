package pl.exaco.receiptApi

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import pl.exaco.receiptApi.exception.*
import pl.exaco.receiptApi.logger.Logger.Companion.logger
import pl.exaco.receiptApi.model.ServiceResponse


@RestControllerAdvice
class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthException::class)
    fun handleException(ex: AuthException): ServiceResponse {
        logger.error("UNAUTHORIZED - HttpStatus: ${ex.message}")
        return ServiceResponse("UNAUTHORIZED", HttpStatus.UNAUTHORIZED.value(), ex.message)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReceiptNotFoundException::class)
    fun handleException(ex: ReceiptNotFoundException): ServiceResponse {
        logger.error("BAD_REQUEST - Receipt not found: ${ex.barcode}")
        return ServiceResponse("BAD_REQUEST", HttpStatus.BAD_REQUEST.value(), ex.localizedMessage)
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ReceiptModelPrepareException::class)
    fun handleException(ex: ReceiptModelPrepareException): ServiceResponse {
        logger.error("SERVICE_ERROR - Receipt preparation failed: ${ex.exMessage}")
        return ServiceResponse("SERVICE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.localizedMessage)
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(UposGetCardException::class)
    fun handleException(ex: UposGetCardException): ServiceResponse {
        logger.error("SERVICE_ERROR - Upos connector problem: ${ex.exMessage}")
        return ServiceResponse("SERVICE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.localizedMessage)
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(UposException::class)
    fun handleException(ex: UposException): ServiceResponse {
        logger.error("SERVICE_ERROR - Upos connector problem: ${ex.exMessage}")
        return ServiceResponse("SERVICE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.localizedMessage)
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(UposCustomerAccountDetailsException::class)
    fun handleException(ex: UposCustomerAccountDetailsException): ServiceResponse {
        logger.error("SERVICE_ERROR - Upos connector problem: ${ex.exMessage}")
        return ServiceResponse("SERVICE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.localizedMessage)
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ServiceResponse {
        logger.error("SERVICE_ERROR - ${ex.localizedMessage}")
        return ServiceResponse("SERVICE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.localizedMessage)
    }

}