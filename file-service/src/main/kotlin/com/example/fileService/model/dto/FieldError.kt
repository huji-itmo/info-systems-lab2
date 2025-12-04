package org.example.model.dto

data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null,
)
