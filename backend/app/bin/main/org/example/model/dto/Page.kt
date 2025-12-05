package org.example.model.dto

import jakarta.json.bind.annotation.JsonbCreator
import jakarta.json.bind.annotation.JsonbProperty

data class Page<T>
    @JsonbCreator
    constructor(
        @param:JsonbProperty("content") val content: List<T>,
        @param:JsonbProperty("page") val page: Int,
        @param:JsonbProperty("size") val size: Int,
        @param:JsonbProperty("totalElements") val totalElements: Long,
        @param:JsonbProperty("totalPages") val totalPages: Int,
    )