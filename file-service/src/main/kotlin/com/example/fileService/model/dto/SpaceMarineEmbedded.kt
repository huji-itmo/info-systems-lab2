package org.example.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.example.fileService.model.AstartesCategory
import com.example.fileService.model.Chapter
import com.example.fileService.model.Coordinates
import com.example.fileService.model.SpaceMarine
import com.example.fileService.model.Weapon
import java.time.LocalDateTime

data class SpaceMarineEmbedded(
    val id: Int,
    val name: String,
    @param:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    val creationDate: LocalDateTime,
    val health: Long,
    val loyal: Boolean?,
    val category: AstartesCategory?,
    val weaponType: Weapon,
    val coordinates: CoordinatesEmbedded,
    val chapter: ChapterEmbedded,
)

data class CoordinatesEmbedded(
    val x: Long,
    val y: Float,
)

data class ChapterEmbedded(
    val name: String,
    val marinesCount: Long,
)

fun SpaceMarine.toEmbedded(coordinates: Coordinates, chapter: Chapter): SpaceMarineEmbedded {
    return SpaceMarineEmbedded(
        id = this.id,
        name = this.name,
        creationDate = this.creationDate,
        health = this.health,
        loyal = this.loyal,
        category = this.category,
        weaponType = this.weaponType,
        coordinates = CoordinatesEmbedded(
            x = coordinates.x,
            y = coordinates.y
        ),
        chapter = ChapterEmbedded(
            name = chapter.name,
            marinesCount = chapter.marinesCount
        )
    )
}
