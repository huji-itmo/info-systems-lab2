package com.example.fileService.model

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class Chapter(
    var id: Long = 0,
    @field:NotBlank
    var name: String,
    @field:Positive
    @field:Max(1000)
    var marinesCount: Long,
)
