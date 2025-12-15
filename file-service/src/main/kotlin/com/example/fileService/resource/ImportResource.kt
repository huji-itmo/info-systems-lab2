package com.example.fileService.resource

import com.example.fileService.beans.ImportHistoryService
import com.example.fileService.beans.MinIOBean
import org.example.shared.model.Coordinates
import com.example.fileService.model.ImportHistory
import org.example.shared.model.SpaceMarine
import org.example.shared.model.dto.ChapterEmbedded
import org.example.shared.model.dto.CoordinatesEmbedded
import com.example.fileService.model.ImportResult
import com.example.fileService.model.ImportSummary
import com.example.fileService.model.SpaceMarineImportRequest
import com.example.fileService.repositories.ChapterRepository
import com.example.fileService.repositories.CoordinatesRepository
import com.example.fileService.repositories.SpaceMarineRepository
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import jakarta.validation.Validator
import org.example.shared.model.Chapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/space-marines")
class ImportResource(
    private val validator: Validator,
    private val minIOBean: MinIOBean,
    private val importHistoryService: ImportHistoryService,
    private val chapterRepository: ChapterRepository,
    private val coordinatesRepository: CoordinatesRepository,
    private val spaceMarineRepository: SpaceMarineRepository
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ImportResource::class.java)
    }

    @PostMapping("/import")
    fun importSpaceMarines(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        val startTime = LocalDateTime.now()
        var uploadedObjectName: String? = null
        var contentType: String? = null
        var historyStatus = ImportHistory.ImportStatus.FAILURE
        var totalRecords: Int? = null
        var successfulCount = 0
        var errorMessage: String? = null

        logger.info("Import request received for file: ${file.originalFilename}")

        try {
            contentType = determineContentType(file.originalFilename)

            uploadedObjectName = minIOBean.uploadFile(file)
            logger.info("File persisted in MinIO as: $uploadedObjectName")

            val importRequests = parseFile(file.inputStream, contentType)
            totalRecords = importRequests.size
            logger.info("Parsed $totalRecords Space Marine records from file")

            validateImportRequests(importRequests)
            val results = processRequests(importRequests)

//            kafkaBean.sendMessage(results.filterIsInstance<ImportResult.Success>().map { it.request })

            successfulCount = results.count { it is ImportResult.Success }
            val failedCount = results.count { it is ImportResult.Failure }
            historyStatus = if (failedCount == 0) ImportHistory.ImportStatus.SUCCESS
            else ImportHistory.ImportStatus.PARTIAL_SUCCESS

            val summary = ImportSummary(
                total = totalRecords,
                successful = successfulCount,
                failed = results.filterIsInstance<ImportResult.Failure>()
            )

            saveImportHistory(
                fileName = file.originalFilename ?: "unknown",
                startTime = startTime,
                minioObjectName = uploadedObjectName,
                contentType = contentType,
                status = historyStatus,
                totalRecords = totalRecords,
                successfulCount = successfulCount,
                failedCount = failedCount
            )

            return ResponseEntity.status(HttpStatus.CREATED).body(summary)

        } catch (e: Exception) {
            errorMessage = e.message ?: "Unknown error"
            logger.error("Import failed for file: ${file.originalFilename}", e)

            saveImportHistory(
                fileName = file.originalFilename ?: "unknown",
                startTime = startTime,
                minioObjectName = uploadedObjectName,
                contentType = contentType,
                status = historyStatus,
                totalRecords = totalRecords,
                successfulCount = successfulCount,
                failedCount = if (totalRecords != null) totalRecords - successfulCount else 0,
                errorMessage = errorMessage
            )

            return when (e) {
                is IllegalArgumentException -> ResponseEntity.badRequest().body(mapOf("error" to errorMessage))
                else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("error" to "Import failed: $errorMessage"))
            }
        }
    }


    private fun determineContentType(filename: String?): String {
        return when {
            filename.isNullOrEmpty() -> throw IllegalArgumentException("Filename cannot be empty")
            filename.endsWith(".json", ignoreCase = true) -> MediaType.APPLICATION_JSON_VALUE
            filename.endsWith(".xml", ignoreCase = true) -> MediaType.APPLICATION_XML_VALUE
            else -> throw IllegalArgumentException("Unsupported file type. Only JSON and XML files are accepted.")
        }
    }

    private fun parseFile(inputStream: InputStream, contentType: String): List<SpaceMarineImportRequest> {
        return try {
            when (contentType) {
                MediaType.APPLICATION_JSON_VALUE -> {
                    val mapper = ObjectMapper().registerKotlinModule()
                    mapper.readValue(inputStream, object : TypeReference<List<SpaceMarineImportRequest>>() {})
                }

                MediaType.APPLICATION_XML_VALUE -> {
                    val xmlMapper = XmlMapper().registerKotlinModule()
                    xmlMapper.readValue(inputStream, object : TypeReference<List<SpaceMarineImportRequest>>() {})
                }

                else -> throw IllegalArgumentException("Unsupported content type: $contentType")
            }
        } catch (e: JsonProcessingException) {
            // Extract core error message without location details
            val cleanMessage = e.message
                ?.substringBefore(" (for ")
                ?.trim()
                ?: "Invalid file structure"
            throw IllegalArgumentException(cleanMessage, e)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse file content: ${e.message ?: "Unknown parsing error"}", e)
        }
    }

    private fun saveImportHistory(
        fileName: String,
        startTime: LocalDateTime,
        minioObjectName: String?,
        contentType: String?,
        status: ImportHistory.ImportStatus,
        totalRecords: Int?,
        successfulCount: Int,
        failedCount: Int,
        errorMessage: String? = null
    ) {
        val history = ImportHistory(
            fileName = fileName,
            minioObjectName = minioObjectName,
            contentType = contentType,
            totalRecords = totalRecords,
            successfulCount = successfulCount,
            failedCount = failedCount,
            timestamp = startTime,
            status = status,
            errorMessage = errorMessage
        )
        importHistoryService.saveHistory(history)
    }

    // Extracted request processing for better readability
    private fun processRequests(requests: List<SpaceMarineImportRequest>): List<ImportResult> {
        return requests.map { request ->
            try {
                processAndSaveRequest(request)
                ImportResult.Success(request)
            } catch (e: Exception) {
                logger.warn("Failed to import ${request.name}: ${e.message}")
                ImportResult.Failure(request.name, e.message ?: "Unknown error")
            }
        }
    }

    private fun validateImportRequests(requests: List<SpaceMarineImportRequest>) {
        if (requests.isEmpty()) {
            throw IllegalArgumentException("File contains no records to import")
        }

        val errors = mutableListOf<String>()

        requests.forEachIndexed { index, request ->
            val violations = validator.validate(request)
            if (violations.isNotEmpty()) {
                errors.add("Record ${index + 1} (${request.name}): ${violations.joinToString { it.message }}")
            }

            when {
                request.chapterId == null && request.chapter == null -> {
                    errors.add("Record ${index + 1} (${request.name}): Must provide either chapterId or chapter object")
                }

                request.chapterId != null && request.chapter != null -> {
                    errors.add("Record ${index + 1} (${request.name}): Cannot provide both chapterId and chapter object")
                }

                request.chapterId != null -> {
                    if (!chapterRepository.existsById(request.chapterId!!)) {
                        errors.add("Record ${index + 1} (${request.name}): Chapter ID ${request.chapterId} does not exist")
                    }
                }

                else -> {
                    val chapterViolations = validator.validate(request.chapter)
                    if (chapterViolations.isNotEmpty()) {
                        errors.add("Record ${index + 1} (${request.name}): Embedded chapter validation failed: ${chapterViolations.joinToString { it.message }}")
                    }
                }
            }

            // Validate coordinates references
            when {
                request.coordinatesId == null && request.coordinates == null -> {
                    errors.add("Record ${index + 1} (${request.name}): Must provide either coordinatesId or coordinates object")
                }

                request.coordinatesId != null && request.coordinates != null -> {
                    errors.add("Record ${index + 1} (${request.name}): Cannot provide both coordinatesId and coordinates object")
                }

                request.coordinatesId != null -> {
                    if (!coordinatesRepository.existsById(request.coordinatesId!!)) {
                        errors.add("Record ${index + 1} (${request.name}): Coordinates ID ${request.coordinatesId} does not exist")
                    }
                }

                else -> {
                    val coordinatesViolations = validator.validate(request.coordinates)
                    if (coordinatesViolations.isNotEmpty()) {
                        errors.add("Record ${index + 1} (${request.name}): Embedded coordinates validation failed: ${coordinatesViolations.joinToString { it.message }}")
                    }
                }
            }
        }

        if (errors.isNotEmpty()) {
            throw ValidationException("Validation failed:\n${errors.joinToString("\n")}")
        }
    }

    @Transactional
    private fun processAndSaveRequest(request: SpaceMarineImportRequest) {
        val chapterId = createOrGetChapter(request.chapter, request.coordinatesId)

        val coordinatesId = createOrGetCoordinates(request.coordinates, request.coordinatesId);

        val spaceMarine = SpaceMarine(
            name = request.name,
            health = request.health,
            category = request.category,
            chapterId = chapterId,
            coordinatesId = coordinatesId,
            weaponType = request.weaponType,
            loyal = request.loyal
        )
        spaceMarineRepository.save(spaceMarine)
    }

    private fun createOrGetChapter(chapterDto: ChapterEmbedded?, chapterId: Long?): Long {
        if (chapterId != null && chapterRepository.findById(chapterId).isPresent) {
            return chapterId;
        }

        if (chapterDto == null) {
            throw IllegalStateException("Chapter and chapter ID null")
        }

        return chapterRepository.save(
            Chapter(
                name = chapterDto.name,
                marinesCount = chapterDto.marinesCount
            )
        ).id
    }

    private fun createOrGetCoordinates(coordinatesDto: CoordinatesEmbedded?, coordinatesId: Long?): Long {
        if (coordinatesId != null && coordinatesRepository.findById(coordinatesId).isPresent) {
            return coordinatesId;
        }

        if (coordinatesDto == null) {
            throw IllegalStateException("Coordinates and CoordinateID null")
        }

        return coordinatesRepository.save(
            Coordinates(
                x = coordinatesDto.x,
                y = coordinatesDto.y
            )
        ).id
    }
}