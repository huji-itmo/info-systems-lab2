package org.example.exceptions

import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response

class NotFoundException(
    message: String,
) : RuntimeException(message)
