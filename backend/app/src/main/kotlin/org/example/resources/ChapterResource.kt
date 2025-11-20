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
import org.example.exceptions.NotFoundException
import org.example.model.Chapter
import org.example.model.dto.Page
import org.example.service.ChapterService
import java.util.logging.Logger
import jakarta.ws.rs.core.StreamingOutput
import org.example.model.dto.createExportResponse

@Path("/chapters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
open class ChapterResource {
    @Inject
    private lateinit var service: ChapterService

    companion object {
        private val logger = Logger.getLogger(ChapterResource::class.java.name)
    }

    @GET
    open fun getAll(
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("size") @DefaultValue("20") size: Int,
    ): Page<Chapter> {
        logger.info("getAll called with page=$page, size=$size")
        require(page >= 0) { "page must be >= 0" }
        require(size in 1..100) { "size must be between 1 and 100" }
        return service.findAll(page, size)
    }

    @POST
    open fun create(
        @Valid chapter: Chapter,
    ): Response {
        logger.info("Received create request: $chapter")
        val saved = service.create(chapter)
        return Response.status(Response.Status.CREATED).entity(saved).build()
    }

    @GET
    @Path("/{id}")
    open fun getById(
        @PathParam("id") id: Long,
    ): Chapter {
        logger.info("getById called with id=$id")
        return service.findById(id)
    }

    @PUT
    @Path("/{id}")
    open fun update(
        @PathParam("id") id: Long,
        @Valid chapter: Chapter,
    ): Chapter {
        logger.info("update called for id=$id with data=$chapter")
        return try {
            service.update(id, chapter)
        } catch (e: NotFoundException) {
            logger.warning("Chapter not found during update: $id")
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
            logger.warning("Chapter not found during delete: $id")
            throw e
        }
        return Response.status(Response.Status.NO_CONTENT).build()
    }

    // Add these imports at the top


    // Add these methods to ChapterResource class
    @GET
    @Path("/export/json")
    @Produces(MediaType.APPLICATION_JSON)
    fun exportJson(
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("size") @DefaultValue("1000") size: Int
    ): Response {
        validateExportPageSize(page, size)
        val pageResult = service.findAll(page, size)

        return createExportResponse(
            data = pageResult.content,
            filename = "chapters_page_${page}_size_${size}.json",
            contentType = MediaType.APPLICATION_JSON
        )
    }

    @GET
    @Path("/export/xml")
    @Produces(MediaType.APPLICATION_XML)
    fun exportXml(
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("size") @DefaultValue("1000") size: Int
    ): Response {
        validateExportPageSize(page, size)
        val pageResult = service.findAll(page, size)

        return createExportResponse(
            data = pageResult.content,
            filename = "chapters_page_${page}_size_${size}.xml",
            contentType = MediaType.APPLICATION_XML
        )
    }

    private fun validateExportPageSize(page: Int, size: Int) {
        require(page >= 0) { "Page must be >= 0" }
        require(size in 1..1000) { "Size must be between 1 and 1000 for exports" }
    }

}
