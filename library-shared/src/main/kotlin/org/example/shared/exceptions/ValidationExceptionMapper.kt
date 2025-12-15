package org.example.exception.mapper

import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.example.shared.model.dto.ErrorResponse
import org.example.shared.model.dto.FieldError

@Provider
class ValidationExceptionMapper : ExceptionMapper<ConstraintViolationException> {
    @Context
    lateinit var uriInfo: UriInfo

    override fun toResponse(exception: ConstraintViolationException): Response {
        val fieldErrors = exception.constraintViolations.map { toFieldError(it) }
        val consolidatedMessage = fieldErrors.joinToString("; ") { "${it.field}: ${it.message}" }

        val errorResponse =
            ErrorResponse(
                error = "VALIDATION_ERROR",
                message = consolidatedMessage,
                path = uriInfo.path,
                fieldErrors = fieldErrors,
            )

        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(errorResponse)
            .build()
    }

    private fun toFieldError(violation: ConstraintViolation<*>): FieldError =
        FieldError(
            field = violation.propertyPath.toString(),
            message = violation.message,
            rejectedValue = violation.invalidValue,
        )
}
