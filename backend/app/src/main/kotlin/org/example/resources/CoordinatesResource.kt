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
import org.example.model.Coordinates
import org.example.model.dto.Page
import org.example.service.CoordinatesService
import java.util.logging.Logger

@Path("/coordinates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
open class CoordinatesResource {
    @Inject
    private lateinit var service: CoordinatesService

    companion object {
        private val logger = Logger.getLogger(CoordinatesResource::class.java.name)
    }

    @GET
    open fun getAll(
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("size") @DefaultValue("20") size: Int,
    ): Page<Coordinates> {
        logger.info("getAll called with page=$page, size=$size")
        require(page >= 0) { "page must be >= 0" }
        require(size in 1..100) { "size must be between 1 and 100" }
        return service.findAll(page, size)
    }

    @POST
    open fun create(
        @Valid coords: Coordinates,
    ): Response {
        logger.info("Received create request: $coords")
        val saved = service.create(coords)
        return Response.status(Response.Status.CREATED).entity(saved).build()
    }

    @GET
    @Path("/{id}")
    open fun getById(
        @PathParam("id") id: Long,
    ): Coordinates {
        logger.info("getById called with id=$id")
        return service.findById(id)
    }

    @PUT
    @Path("/{id}")
    open fun update(
        @PathParam("id") id: Long,
        @Valid coords: Coordinates,
    ): Coordinates {
        logger.info("update called for id=$id with data=$coords")
        return try {
            service.update(id, coords)
        } catch (e: NotFoundException) {
            logger.warning("Coordinates not found during update: $id")
            throw e
        }
    }

    @DELETE
    @Path("/{id}")
    open fun delete(
        @PathParam("id") id: Long,
    ): Response {
        logger.info("delete called for id=$id")
        try {
            service.delete(id)
        } catch (e: NotFoundException) {
            logger.warning("Coordinates not found during delete: $id")
            throw e
        }
        return Response.status(Response.Status.NO_CONTENT).build()
    }

    @GET
    @Path("/export/json")
    @Produces(MediaType.APPLICATION_JSON)
    fun exportJson(
        @QueryParam("page") @DefaultValue("0") page: Int, @QueryParam("size") @DefaultValue("1000") size: Int
    ): Response {
        validateExportPageSize(page, size)
        val pageResult = service.findAll(page, size)

        return createExportResponse(
            data = pageResult.content,
            filename = "coordinates_page_${page}_size_${size}.json",
            contentType = MediaType.APPLICATION_JSON
        )
    }

    @GET
    @Path("/export/xml")
    @Produces(MediaType.APPLICATION_XML)
    fun exportXml(
        @QueryParam("page") @DefaultValue("0") page: Int, @QueryParam("size") @DefaultValue("1000") size: Int
    ): Response {
        validateExportPageSize(page, size)
        val pageResult = service.findAll(page, size)

        return createExportResponse(
            data = pageResult.content,
            filename = "coordinates_page_${page}_size_${size}.xml",
            contentType = MediaType.APPLICATION_XML
        )
    }

    private fun validateExportPageSize(page: Int, size: Int) {
        require(page >= 0) { "Page must be >= 0" }
        require(size in 1..1000) { "Size must be between 1 and 1000 for exports" }
    }

    private fun <T> createExportResponse(
        data: List<T>, filename: String, contentType: String
    ): Response {
        val streamingOutput = StreamingOutput { output ->
            when (contentType) {
                MediaType.APPLICATION_JSON -> {
                    val mapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                    mapper.writeValue(output, data)
                }

                MediaType.APPLICATION_XML -> {
                    val xmlMapper = XmlMapper()
                    xmlMapper.writeValue(output, data)
                }

                else -> throw IllegalArgumentException("Unsupported content type: $contentType")
            }
        }

        return Response.ok(streamingOutput).header("Content-Disposition", "attachment; filename=\"$filename\"")
            .header("Content-Type", contentType).build()
    }
}
