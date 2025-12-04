package org.example.exception.handler

import com.example.fileService.exceptions.NotFoundException
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.example.model.dto.ErrorResponse
import org.example.model.dto.FieldError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.lang.IllegalArgumentException

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = "INVALID_INPUT",
            message = ex.message ?: "Invalid input provided",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(
        ex: NotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = "NOT_FOUND",
            message = ex.message ?: "Requested resource was not found",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val fieldErrors = ex.constraintViolations.map(::toFieldError)
        val consolidatedMessage = fieldErrors.joinToString("; ") { "${it.field}: ${it.message}" }

        val error = ErrorResponse(
            error = "VALIDATION_ERROR",
            message = consolidatedMessage,
            path = request.getDescription(false).removePrefix("uri="),
            fieldErrors = fieldErrors
        )
        return ResponseEntity.badRequest().body(error)
    }

    private fun toFieldError(violation: ConstraintViolation<*>): FieldError {
        return FieldError(
            field = violation.propertyPath.toString(),
            message = violation.message,
            rejectedValue = violation.invalidValue?.toString()
        )
    }
}