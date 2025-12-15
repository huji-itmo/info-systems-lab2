package org.example.shared.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.StreamingOutput
import org.example.shared.model.AstartesCategory
import org.example.shared.model.Chapter
import org.example.shared.model.Coordinates
import org.example.shared.model.SpaceMarine
import org.example.shared.model.Weapon
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

fun <T> createExportResponse(
    data: List<T>,
    filename: String,
    contentType: String
): Response {
    val streamingOutput = StreamingOutput { output ->
        when (contentType) {
            MediaType.APPLICATION_JSON -> {
                val mapper = ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .registerModule(JavaTimeModule()) // Register JavaTimeModule
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // Format as ISO strings

                mapper.writeValue(output, data)
            }

            MediaType.APPLICATION_XML -> {
                val xmlMapper = XmlMapper()
                    .registerModule(JavaTimeModule()) // Register JavaTimeModule for XML too
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

                xmlMapper.writeValue(output, data)
            }

            else -> throw IllegalArgumentException("Unsupported content type: $contentType")
        }
    }

    return Response.ok(streamingOutput)
        .header("Content-Disposition", "attachment; filename=\"$filename\"")
        .header("Content-Type", contentType)
        .build()
}