package org.example.model.dto

import org.example.JsonDeserializable

@JsonDeserializable
data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null,
)
