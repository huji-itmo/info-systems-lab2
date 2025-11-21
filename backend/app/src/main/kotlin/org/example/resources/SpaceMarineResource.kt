package org.example.resources

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.validation.Valid
import jakarta.validation.Validator
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
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.StreamingOutput
import org.example.exceptions.NotFoundException
import org.example.model.Chapter
import org.example.model.Coordinates
import org.example.model.dto.Page
import org.example.model.SpaceMarine
import org.example.model.dto.ImportResult
import org.example.model.dto.ImportSummary
import org.example.model.dto.SpaceMarineCreateRequest
import org.example.model.dto.SpaceMarineImportRequest
import org.example.model.dto.SpaceMarineUpdateRequest
import org.example.model.dto.createExportResponse
import org.example.model.dto.toEmbedded
import org.example.service.ChapterService
import org.example.service.CoordinatesService
import org.example.service.SpaceMarineService
import org.glassfish.jersey.media.multipart.FormDataContentDisposition
import org.glassfish.jersey.media.multipart.FormDataParam
import java.io.InputStream
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

    @Inject
    private lateinit var validator: Validator


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

    @PUT
    @Path("/{id}")
    open fun update(
        @PathParam("id") id: Int,
        @Valid update: SpaceMarineUpdateRequest,
    ): SpaceMarine {
        logger.info("UPDATE REQUEST for ID $id: $update")
        // Validate existence of referenced entities only if provided in update
        validateCoordinatesAndChapter(
            coordinatesId = update.coordinatesId,
            chapterId = update.chapterId,
        )
        return spaceMarineService.update(id, update)
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


    @POST
    @Path("/import")
    @Consumes(MULTIPART_FORM_DATA)
    fun importSpaceMarines(
        @FormDataParam("file") fileInputStream: InputStream,
        @FormDataParam("file") fileDetail: FormDataContentDisposition
    ): Response {
        logger.info("Import request received for file: ${fileDetail.fileName}")

        try {
            val contentType = determineContentType(fileDetail.fileName)
            val importRequests = parseFile(fileInputStream, contentType)

            logger.info("Parsed ${importRequests.size} Space Marine records from file")
            validateImportRequests(importRequests)

            val results = importRequests.map { request ->
                try {
                    processImportRequest(request)
                    ImportResult.Success(request.name)
                } catch (e: Exception) {
                    logger.warning("Failed to import ${request.name}: ${e.message}")
                    ImportResult.Failure(request.name, e.message ?: "Unknown error")
                }
            }

            val summary = ImportSummary(
                total = results.size,
                successful = results.count { it is ImportResult.Success },
                failed = results.filterIsInstance<ImportResult.Failure>()
            )

            return Response.status(Response.Status.CREATED)
                .entity(summary)
                .build()

        } catch (e: Exception) {
            logger.severe("Import failed: ${e.message}")
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to "Import failed: ${e.message}"))
                .build()
        }
    }

    private fun determineContentType(filename: String): String {
        return when {
            filename.endsWith(".json", ignoreCase = true) -> MediaType.APPLICATION_JSON
            filename.endsWith(".xml", ignoreCase = true) -> MediaType.APPLICATION_XML
            else -> throw WebApplicationException("Unsupported file type. Only JSON and XML files are accepted.",
                Response.Status.BAD_REQUEST
            ) as Throwable
        }
    }

    private fun parseFile(inputStream: InputStream, contentType: String): List<SpaceMarineImportRequest> {
        return when (contentType) {
            MediaType.APPLICATION_JSON -> {
                val mapper = ObjectMapper().registerKotlinModule()
                mapper.readValue(inputStream, object : TypeReference<List<SpaceMarineImportRequest>>() {})
            }
            MediaType.APPLICATION_XML -> {
                val xmlMapper = XmlMapper().registerKotlinModule()
                xmlMapper.readValue(inputStream, object : TypeReference<List<SpaceMarineImportRequest>>() {})
            }
            else -> throw IllegalArgumentException("Unsupported content type: $contentType")
        }
    }

    private fun validateImportRequests(requests: List<SpaceMarineImportRequest>) {
        if (requests.isEmpty()) {
            throw WebApplicationException("File contains no records to import", Response.Status.BAD_REQUEST)
        }

        val errors = mutableListOf<String>()

        requests.forEachIndexed { index, request ->
            val violations = validator.validate(request)
            if (violations.isNotEmpty()) {
                errors.add("Record ${index + 1} (${request.name}): ${violations.joinToString { it.message }}")
            }

            // Validate coordinates references
            if (request.coordinatesId == null && request.coordinates == null) {
                errors.add("Record ${index + 1} (${request.name}): Must provide either coordinatesId or coordinates object")
            }
            if (request.coordinatesId != null && request.coordinates != null) {
                errors.add("Record ${index + 1} (${request.name}): Cannot provide both coordinatesId and coordinates object")
            }

            // Validate chapter references
            if (request.chapterId == null && request.chapter == null) {
                errors.add("Record ${index + 1} (${request.name}): Must provide either chapterId or chapter object")
            }
            if (request.chapterId != null && request.chapter != null) {
                errors.add("Record ${index + 1} (${request.name}): Cannot provide both chapterId and chapter object")
            }
        }

        if (errors.isNotEmpty()) {
            throw WebApplicationException(
                "Validation failed:\n${errors.joinToString("\n")}",
                Response.Status.BAD_REQUEST
            ) as Throwable
        }
    }

    private fun processImportRequest(request: SpaceMarineImportRequest): SpaceMarine {
        // Resolve coordinates
        val coordinatesId = when {
            request.coordinatesId != null -> {
                // Validate existing coordinates
                try {
                    coordinatesService.findById(request.coordinatesId).id
                } catch (e: NotFoundException) {
                    throw NotFoundException("Coordinates ID ${request.coordinatesId} not found for ${request.name}")
                }
            }
            request.coordinates != null -> {
                // Create new coordinates
                val newCoords = coordinatesService.create(
                    Coordinates(
                        x = request.coordinates.x,
                        y = request.coordinates.y
                    )
                )
                newCoords.id
            }
            else -> throw IllegalArgumentException("No coordinates reference provided for ${request.name}")
        }

        // Resolve chapter
        val chapterId = when {
            request.chapterId != null -> {
                // Validate existing chapter
                try {
                    chapterService.findById(request.chapterId).id
                } catch (e: NotFoundException) {
                    throw NotFoundException("Chapter ID ${request.chapterId} not found for ${request.name}")
                }
            }
            request.chapter != null -> {
                // Create new chapter
                val newChapter = chapterService.create(
                    Chapter(
                        name = request.chapter.name,
                        marinesCount = request.chapter.marinesCount
                    )
                )
                newChapter.id
            }
            else -> throw IllegalArgumentException("No chapter reference provided for ${request.name}")
        }

        // Create space marine
        return spaceMarineService.create(
            SpaceMarineCreateRequest(
                name = request.name,
                coordinatesId = coordinatesId,
                chapterId = chapterId,
                health = request.health,
                loyal = request.loyal,
                category = request.category.toString(),
                weaponType = request.weaponType.toString()
            )
        )
    }
}
