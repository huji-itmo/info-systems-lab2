package com.example.fileService.model

import jakarta.validation.constraints.Max


data class Coordinates(
    var id: Long = 0,
    var x: Long,
    @field:Max(343)
    var y: Float,
)
