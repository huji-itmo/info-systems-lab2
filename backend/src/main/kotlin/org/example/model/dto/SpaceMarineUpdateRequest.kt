package org.example.model.dto

import jakarta.json.bind.annotation.JsonbCreator
import jakarta.json.bind.annotation.JsonbProperty

data class SpaceMarineUpdateRequest
    @JsonbCreator
    constructor(
        @param:JsonbProperty("name") val name: String?,
        @param:JsonbProperty("coordinatesId") val coordinatesId: Long?,
        @param:JsonbProperty("chapterId") val chapterId: Long?,
        @param:JsonbProperty("health") val health: Long?,
        @param:JsonbProperty("loyal") val loyal: Boolean?,
        @param:JsonbProperty("category") val category: String?,
        @param:JsonbProperty("weaponType") val weaponType: String,
    )
