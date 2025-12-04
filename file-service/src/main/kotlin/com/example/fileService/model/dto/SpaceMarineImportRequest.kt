package com.example.fileService.model.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import com.example.fileService.model.AstartesCategory
import com.example.fileService.model.Weapon
import org.example.model.dto.ChapterEmbedded
import org.example.model.dto.CoordinatesEmbedded

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpaceMarineImportRequest(
    @get:NotBlank(message = "Name is required")
    val name: String,

    // Either provide coordinatesId OR coordinates object
    val coordinatesId: Long? = null,
    @get:Valid
    val coordinates: CoordinatesEmbedded? = null,

    // Either provide chapterId OR chapter object
    val chapterId: Long? = null,
    @get:Valid
    val chapter: ChapterEmbedded? = null,

    @get:NotNull(message = "Health is required")
    @get:Positive(message = "Health must be positive")
    val health: Long,

    val loyal: Boolean? = null,
    val category: AstartesCategory? = null,

    @get:NotNull(message = "Weapon type is required")
    val weaponType: Weapon
)

sealed class ImportResult(open val name: String) {
    data class Success(override val name: String) : ImportResult(name)
    data class Failure(override val name: String, val reason: String) : ImportResult(name)
}

data class ImportSummary(
    val total: Int,
    val successful: Int,
    val failed: List<ImportResult.Failure>
)