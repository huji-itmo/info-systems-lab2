package org.example.shared.model.dto

import org.example.shared.JsonDeserializable

@JsonDeserializable
data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null,
)
