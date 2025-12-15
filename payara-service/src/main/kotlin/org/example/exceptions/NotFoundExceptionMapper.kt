package org.example.exceptions

import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.example.shared.model.dto.ErrorResponse

@Provider
class NotFoundExceptionMapper : ExceptionMapper<NotFoundException> {
    @Context
    lateinit var uriInfo: UriInfo

    override fun toResponse(exception: NotFoundException): Response {
        val error =
            ErrorResponse(
                error = "NOT_FOUND",
                message = exception.message ?: "Requested resource was not found",
                path = uriInfo.path,
            )
        return Response
            .status(Response.Status.NOT_FOUND)
            .entity(error)
            .build()
    }
}
