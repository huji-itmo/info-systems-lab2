package org.example.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.example.exceptions.NotFoundException
import org.example.model.AstartesCategory
import org.example.model.Chapter
import org.example.model.Coordinates
import org.example.model.dto.Page
import org.example.model.SpaceMarine
import org.example.model.Weapon
import org.example.model.dto.SpaceMarineCreateRequest
import org.example.model.dto.SpaceMarineImportRequest
import org.example.model.dto.SpaceMarineUpdateRequest
import java.util.logging.Logger
import kotlin.math.ceil
import kotlin.toString

@ApplicationScoped
open class SpaceMarineService {
    @PersistenceContext(unitName = "my-pu")
    private lateinit var em: EntityManager

    @Inject
    private lateinit var chapterService: ChapterService

    @Inject
    private lateinit var coordinatesService: CoordinatesService

    @Inject
    private lateinit var service: ChapterService

    companion object {
        private val logger = Logger.getLogger(SpaceMarineService::class.java.name)
    }

    open fun findAll(
        page: Int,
        size: Int,
    ): Page<SpaceMarine> {
        val total = em.createQuery("SELECT COUNT(s) FROM SpaceMarine s", Long::class.java).singleResult
        val content =
            em
                .createQuery("SELECT s FROM SpaceMarine s", SpaceMarine::class.java)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .resultList

        return Page(
            content = content,
            page = page,
            size = size,
            totalElements = total,
            totalPages = ceil(total.toDouble() / size).toInt(),
        )
    }

    open fun findById(id: Int): SpaceMarine =
        em.find(SpaceMarine::class.java, id) ?: throw NotFoundException("SpaceMarine not found with id: $id")

    @Transactional
    open fun create(
        @Valid spaceMarine: SpaceMarineCreateRequest,
    ): SpaceMarine {
        // Convert string values to enums safely
        val category =
            spaceMarine.category?.let {
                try {
                    if (it == "null") {
                        return@let null
                    }
                    AstartesCategory.valueOf(it.uppercase())
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException(
                        "Invalid category value: $it. " +
                            "Valid values are: ${AstartesCategory.entries.joinToString()}",
                    )
                }
            }

        val weaponType =
            try {
                Weapon.valueOf(spaceMarine.weaponType.uppercase())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException(
                    "Invalid weapon type: ${spaceMarine.weaponType}. " +
                        "Valid values are: ${Weapon.entries.joinToString()}",
                )
            }

        val entity =
            SpaceMarine(
                id = 0,
                name = spaceMarine.name,
                coordinatesId = spaceMarine.coordinatesId,
                chapterId = spaceMarine.chapterId,
                health = spaceMarine.health,
                loyal = spaceMarine.loyal,
                category = category,
                weaponType = weaponType,
            )
        em.persist(entity)
        return entity
    }

    @Transactional
    open fun update(
        id: Int,
        @Valid update: SpaceMarineUpdateRequest,
    ): SpaceMarine {
        val existing = findById(id)

        // Convert string values to enums with null safety
        val category =
            update.category?.let { categoryStr ->
                try {
                    AstartesCategory.valueOf(categoryStr.uppercase())
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException(
                        "Invalid category value: $categoryStr. " +
                            "Valid values are: ${AstartesCategory.values().joinToString()}",
                    )
                }
            } ?: existing.category

        val weaponType =
            update.weaponType?.let { weaponStr ->
                try {
                    Weapon.valueOf(weaponStr.uppercase())
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException(
                        "Invalid weapon type: $weaponStr. " +
                            "Valid values are: ${Weapon.values().joinToString()}",
                    )
                }
            } ?: existing.weaponType

        val updated =
            SpaceMarine(
                id = id,
                name = update.name ?: existing.name,
                coordinatesId = update.coordinatesId ?: existing.coordinatesId,
                chapterId = update.chapterId ?: existing.chapterId,
                health = update.health ?: existing.health,
                loyal = update.loyal ?: existing.loyal,
                category = category,
                weaponType = weaponType,
                creationDate = existing.creationDate, // immutable
            )
        em.merge(updated)
        return updated
    }

    @Transactional
    open fun delete(id: Int) {
        val entity = findById(id)
        em.remove(entity)
    }

    @Transactional
    open fun assignMarineToChapter(
        marineId: Int,
        chapterId: Long,
    ): SpaceMarine {
        logger.info("Assigning marine $marineId to chapter $chapterId")
        val marine = findById(marineId)

        // Валидация существования ордена
        try {
            chapterService.findById(chapterId)
        } catch (e: NotFoundException) {
            throw NotFoundException("Chapter with ID $chapterId not found")
        }

        marine.chapterId = chapterId
        return em.merge(marine)
    }

    open fun findByWeaponTypes(
        weaponTypes: List<Weapon>,
        page: Int,
        size: Int,
    ): Page<SpaceMarine> {
        logger.info("Filtering marines by weapons: $weaponTypes (page=$page, size=$size)")

        if (weaponTypes.isEmpty()) {
            return Page(emptyList(), page, size, 0, 0)
        }

        // Запрос для получения общего количества
        val countQuery =
            em
                .createQuery(
                    "SELECT COUNT(s) FROM SpaceMarine s WHERE s.weaponType IN :weapons",
                    Long::class.java,
                ).setParameter("weapons", weaponTypes)

        val total = countQuery.singleResult

        // Запрос для получения данных с пагинацией
        val contentQuery =
            em.createQuery(
                "SELECT s FROM SpaceMarine s WHERE s.weaponType IN :weapons ORDER BY s.name",
                SpaceMarine::class.java,
            )
        contentQuery.setParameter("weapons", weaponTypes)
        contentQuery.setFirstResult(page * size)
        contentQuery.setMaxResults(size)

        val content = contentQuery.resultList

        return Page(
            content = content,
            page = page,
            size = size,
            totalElements = total,
            totalPages = ceil(total.toDouble() / size).toInt(),
        )
    }

    open fun sumHealth(): Long {
        logger.info("Calculating sum of health values")
        return em
            .createQuery("SELECT COALESCE(SUM(s.health), 0) FROM SpaceMarine s", Long::class.java)
            .singleResult
    }

    open fun averageHealth(): Double {
        logger.info("Calculating average health value")
        val avg = em.createQuery("SELECT AVG(s.health) FROM SpaceMarine s", Double::class.java).singleResult
        return avg ?: 0.0
    }

    open fun processImportRequest(request: SpaceMarineImportRequest): SpaceMarine {
        val coordinatesId = when {
            request.coordinatesId != null -> {
                request.coordinatesId
            }
            request.coordinates != null -> {
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

        val chapterId = when {
            request.chapterId != null -> {
                request.chapterId
            }
            request.chapter != null -> {
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

        return create(
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
