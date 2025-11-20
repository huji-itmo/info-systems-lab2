package org.example.exceptions

import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.example.model.dto.ErrorResponse

@Provider
class IllegalArgumentExceptionMapper : ExceptionMapper<IllegalArgumentException> {
    @Context
    lateinit var uriInfo: UriInfo

    override fun toResponse(exception: IllegalArgumentException): Response {
        val error =
            ErrorResponse(
                error = "INVALID_INPUT",
                message = exception.message ?: "Invalid input provided",
                path = uriInfo.path,
            )
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(error)
            .build()
    }
}
