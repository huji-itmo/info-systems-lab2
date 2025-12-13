package org.example.model.dto

import jakarta.json.bind.annotation.JsonbCreator
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class SpaceMarineCreateRequest
@JsonbCreator
constructor(
    @field:NotBlank
    @param:JsonbProperty("name")
    val name: String,
    @field:NotNull
    @field:Positive
    @param:JsonbProperty("coordinatesId")
    val coordinatesId: Long,
    @field:NotNull
    @field:Positive
    @param:JsonbProperty("chapterId")
    val chapterId: Long,
    @field:NotNull
    @field:Positive
    @param:JsonbProperty("health")
    val health: Long,
    @param:JsonbProperty("loyal")
    val loyal: Boolean?,
    @param:JsonbProperty("category")
    val category: String?,
    @field:NotNull
    @param:JsonbProperty("weaponType")
    val weaponType: String,
)
