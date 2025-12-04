package com.example.fileService.resource

import com.example.fileService.beans.MinIOBean
import com.example.fileService.model.dto.ImportResult
import com.example.fileService.model.dto.ImportSummary
import com.example.fileService.model.dto.SpaceMarineImportRequest
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.validation.ValidationException
import jakarta.validation.Validator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.*
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

@RestController
@RequestMapping("/api/space-marines")
class ImportResource(
    private val validator: Validator,
    private val minIOBean: MinIOBean
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ImportResource::class.java)
    }

    @PostMapping("/import")
    fun importSpaceMarines(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        logger.info("Import request received for file: ${file.originalFilename}")

        try {
            val uploadedObjectName = minIOBean.uploadFile(file)
            logger.info("File persisted in MinIO as: $uploadedObjectName")

            val contentType = determineContentType(file.originalFilename)
            val importRequests = parseFile(file.inputStream, contentType)

            logger.info("Parsed ${importRequests.size} Space Marine records from file")
            validateImportRequests(importRequests)

            val results = importRequests.map { request ->
                try {
                    processImportRequest(request)
                    ImportResult.Success(request.name)
                } catch (e: Exception) {
                    logger.warn("Failed to import ${request.name}: ${e.message}")
                    ImportResult.Failure(request.name, e.message ?: "Unknown error")
                }
            }

            val summary = ImportSummary(
                total = results.size,
                successful = results.count { it is ImportResult.Success },
                failed = results.filterIsInstance<ImportResult.Failure>()
            )

            return ResponseEntity.status(HttpStatus.CREATED).body(summary)

        } catch (e: IllegalArgumentException) {
            logger.warn("Client error during import: ${e.message}")
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("Unexpected error during import", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Import failed: ${e.message}"))
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
            throw ValidationException("Validation failed:\n${errors.joinToString("\n")}")
        }
    }

    private fun processImportRequest(request: SpaceMarineImportRequest) {
        // Implementation would go here
    }
}