package org.example.resources

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.StreamingOutput
import org.example.exceptions.NotFoundException
import org.example.model.dto.Page
import org.example.model.SpaceMarine
import org.example.model.dto.SpaceMarineCreateRequest
import org.example.model.dto.SpaceMarineUpdateRequest
import org.example.model.dto.createExportResponse
import org.example.model.dto.toEmbedded
import org.example.service.ChapterService
import org.example.service.CoordinatesService
import org.example.service.SpaceMarineService
import java.util.logging.Logger

@Path("/space-marines")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
open class SpaceMarineResource {
    @Inject
    private lateinit var spaceMarineService: SpaceMarineService

    @Inject
    private lateinit var coordinatesService: CoordinatesService

    @Inject
    private lateinit var chapterService: ChapterService

    companion object {
        private val logger = Logger.getLogger(SpaceMarineResource::class.java.name)
    }


    @GET
    @Path("/{id}")
    open fun getById(
        @PathParam("id") id: Int,
        @QueryParam("embed") embed: String? = null // Accepts "all" or comma-separated values
    ): Any {
        val spaceMarine = spaceMarineService.findById(id)

        // Parse embed parameter
        val embedSet = parseEmbedParam(embed)

        // Return embedded version if requested
        if (embedSet.contains("all") || embedSet.contains("coordinates") || embedSet.contains("chapter")) {
            val coordinates = coordinatesService.findById(spaceMarine.coordinatesId)
            val chapter = chapterService.findById(spaceMarine.chapterId)
            return spaceMarine.toEmbedded(coordinates, chapter)
        }

        // Default behavior - return standard entity
        return spaceMarine
    }

    @GET
    open fun getAll(
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("size") @DefaultValue("20") size: Int,
        @QueryParam("embed") embed: String? = null
    ): Any {
        logger.info("getAll called with page=$page, size=$size, embed=$embed")
        require(page >= 0) { "page must be >= 0" }
        require(size in 1..100) { "size must be between 1 and 100" }

        val pageResult = spaceMarineService.findAll(page, size)
        val embedSet = parseEmbedParam(embed)

        // Return embedded version if requested
        if (embedSet.contains("all") || embedSet.contains("coordinates") || embedSet.contains("chapter")) {
            val embeddedContent = pageResult.content.map { spaceMarine ->
                val coordinates = coordinatesService.findById(spaceMarine.coordinatesId)
                val chapter = chapterService.findById(spaceMarine.chapterId)
                spaceMarine.toEmbedded(coordinates, chapter)
            }
            return Page(
                content = embeddedContent,
                totalElements = pageResult.totalElements,
                totalPages = pageResult.totalPages,
                page = pageResult.page,
                size = pageResult.size
            )
        }

        // Default behavior
        return pageResult
    }

    // Helper to parse embed parameter
    private fun parseEmbedParam(embed: String?): Set<String> {
        return embed?.split(",")?.map { it.trim().lowercase() }?.toSet() ?: emptySet()
    }

    @POST
    fun create(
        @Valid spaceMarine: SpaceMarineCreateRequest,
    ): SpaceMarine {
        logger.info("Received create request: $spaceMarine")
        logger.info(
            "Name: ${spaceMarine.name}, " +
                    "CoordinatesId: ${spaceMarine.coordinatesId}, " +
                    "Weapon: ${spaceMarine.weaponType}",
        )

        // Validate existence of referenced entities
        validateCoordinatesAndChapter(
            coordinatesId = spaceMarine.coordinatesId,
            chapterId = spaceMarine.chapterId,
        )

        return spaceMarineService.create(spaceMarine)
    }

    @DELETE
    @Path("/{id}")
    open fun delete(
        @PathParam("id") id: Int,
    ): Response {
        spaceMarineService.delete(id)
        return Response.status(Response.Status.NO_CONTENT).build()
    }

    private fun validateCoordinatesAndChapter(
        coordinatesId: Long?,
        chapterId: Long?,
    ) {
        coordinatesId?.let {
            try {
                coordinatesService.findById(it)
            } catch (e: NotFoundException) {
                throw NotFoundException("Coordinates with ID $it not found")
            }
        }
        chapterId?.let {
            try {
                chapterService.findById(it)
            } catch (e: NotFoundException) {
                throw NotFoundException("Chapter with ID $it not found")
            }
        }
    }

    @GET
    @Path("/health/sum")
    fun calculateHealthSum(): Long {
        logger.info("Handling health sum request")
        return spaceMarineService.sumHealth()
    }

    @GET
    @Path("/health/average")
    fun calculateHealthAverage(): Double {
        logger.info("Handling health average request")
        return spaceMarineService.averageHealth()
    }

    @GET
    @Path("/export/json")
    @Produces(MediaType.APPLICATION_JSON)
    fun exportJson(
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("size") @DefaultValue("1000") size: Int,
        @QueryParam("embed") embed: String? = null
    ): Response {
        validateExportPageSize(page, size)

        // Get data based on embed parameter
        val data = if (parseEmbedParam(embed).isNotEmpty()) {
            val pageResult = spaceMarineService.findAll(page, size)
            pageResult.content.map { spaceMarine ->
                val coordinates = coordinatesService.findById(spaceMarine.coordinatesId)
                val chapter = chapterService.findById(spaceMarine.chapterId)
                spaceMarine.toEmbedded(coordinates, chapter)
            }
        } else {
            spaceMarineService.findAll(page, size).content
        }

        return createExportResponse(
            data = data,
            filename = "space_marines_page_${page}_size_${size}.json",
            contentType = MediaType.APPLICATION_JSON
        )
    }

    @GET
    @Path("/export/xml")
    @Produces(MediaType.APPLICATION_XML)
    fun exportXml(
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("size") @DefaultValue("1000") size: Int,
        @QueryParam("embed") embed: String? = null
    ): Response {
        validateExportPageSize(page, size)

        // Get data based on embed parameter
        val data = if (parseEmbedParam(embed).isNotEmpty()) {
            val pageResult = spaceMarineService.findAll(page, size)
            pageResult.content.map { spaceMarine ->
                val coordinates = coordinatesService.findById(spaceMarine.coordinatesId)
                val chapter = chapterService.findById(spaceMarine.chapterId)
                spaceMarine.toEmbedded(coordinates, chapter)
            }
        } else {
            spaceMarineService.findAll(page, size).content
        }

        return createExportResponse(
            data = data,
            filename = "space_marines_page_${page}_size_${size}.xml",
            contentType = MediaType.APPLICATION_XML
        )
    }

    private fun validateExportPageSize(page: Int, size: Int) {
        require(page >= 0) { "Page must be >= 0" }
        require(size in 1..1000) { "Size must be between 1 and 1000 for exports" }
    }

}
