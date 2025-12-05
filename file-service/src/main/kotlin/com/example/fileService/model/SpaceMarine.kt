package com.example.fileService.model

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

data class SpaceMarine(
    var id: Int = 0,
    @field:NotBlank
    var name: String,
    @field:NotNull
    var coordinatesId: Long,
    @field:NotNull
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    var creationDate: LocalDateTime = LocalDateTime.now(),
    @field:NotNull
    var chapterId: Long,
    @field:NotNull
    @field:Positive
    var health: Long,
    var loyal: Boolean? = null,
    var category: AstartesCategory? = null,
    @field:NotNull
    var weaponType: Weapon,
)
