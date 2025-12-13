package org.example.resources

import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.example.model.dto.Page
import org.example.model.SpaceMarine
import org.example.model.Weapon
import org.example.service.ChapterService
import org.example.service.SpaceMarineService
import java.util.logging.Logger

@Path("/special-operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
open class SpecialOperationsResource {
    @Inject
    private lateinit var spaceMarineService: SpaceMarineService

    @Inject
    private lateinit var chapterService: ChapterService

    companion object {
        private val logger = Logger.getLogger(SpecialOperationsResource::class.java.name)
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
    @Path("/space-marines/filter-by-weapons")
    fun filterByWeaponTypes(
        @QueryParam("weaponTypes[]") weaponTypesParam: List<String>,
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("size") @DefaultValue("20") size: Int,
    ): Page<SpaceMarine> {
        logger.info("Filtering marines by weapons: $weaponTypesParam (page=$page, size=$size)")

        validatePaginationParams(page, size)

        val validWeapons = validateAndConvertWeapons(weaponTypesParam)

        return spaceMarineService.findByWeaponTypes(validWeapons, page, size)
    }

    private fun validatePaginationParams(
        page: Int,
        size: Int,
    ) {
        require(page >= 0) { "Page number must be >= 0" }
        require(size in 1..100) { "Page size must be between 1 and 100" }
    }

    private fun validateAndConvertWeapons(weaponTypes: List<String>): List<Weapon> {
        if (weaponTypes.isEmpty()) {
            throw WebApplicationException(
                "At least one weapon type must be specified",
                Response.Status.BAD_REQUEST,
            )
        }

        if (weaponTypes.size > 10) {
            throw WebApplicationException(
                "Maximum 10 weapon types allowed in a single request",
                Response.Status.BAD_REQUEST,
            )
        }

        val validWeapons = mutableListOf<Weapon>()
        val invalidWeapons = mutableListOf<String>()

        weaponTypes.forEach { weaponStr ->
            try {
                validWeapons.add(Weapon.valueOf(weaponStr.uppercase()))
            } catch (e: IllegalArgumentException) {
                invalidWeapons.add(weaponStr)
            }
        }

        if (invalidWeapons.isNotEmpty()) {
            throw WebApplicationException(
                "Invalid weapon types: ${invalidWeapons.joinToString()}. " +
                    "Valid values: ${Weapon.values().joinToString { it.name }}",
                Response.Status.BAD_REQUEST,
            )
        }

        return validWeapons
    }

    @PUT
    @Path("/chapters/{chapterId}/assign-marine/{marineId}")
    fun assignMarineToChapter(
        @PathParam("chapterId") chapterId: Long,
        @PathParam("marineId") marineId: Int,
    ): SpaceMarine {
        logger.info("Assigning marine $marineId to chapter $chapterId")
        return spaceMarineService.assignMarineToChapter(marineId, chapterId)
    }
}
