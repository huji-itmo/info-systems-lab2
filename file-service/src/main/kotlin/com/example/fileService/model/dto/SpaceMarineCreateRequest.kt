package org.example.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class SpaceMarineCreateRequest
constructor(
    @field:NotBlank
    val name: String,
    @field:NotNull
    @field:Positive
    val coordinatesId: Long,
    @field:NotNull
    @field:Positive
    val chapterId: Long,
    @field:NotNull
    @field:Positive
    val health: Long,
    val loyal: Boolean?,
    val category: String?,
    @field:NotNull
    val weaponType: String,
)
