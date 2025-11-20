package org.example.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import org.example.JsonDeserializable
import java.time.LocalDateTime

@JsonDeserializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val path: String,
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val fieldErrors: List<FieldError>? = null,
)