package org.example.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.example.exceptions.NotFoundException
import org.example.shared.model.Chapter
import org.example.shared.model.dto.Page
import kotlin.math.ceil

@ApplicationScoped
open class ChapterService {
    @PersistenceContext(unitName = "my-pu")
    private lateinit var em: EntityManager

    open fun findAll(
        page: Int,
        size: Int,
    ): Page<Chapter> {
        val total = em.createQuery("SELECT COUNT(c) FROM Chapter c", Long::class.java).singleResult
        val content =
            em
                .createQuery("SELECT c FROM Chapter c", Chapter::class.java)
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

    open fun findById(id: Long): Chapter = em.find(Chapter::class.java, id) ?: throw NotFoundException("Chapter not found with id: $id")

    @Transactional
    open fun create(
        @Valid chapter: Chapter,
    ): Chapter {
        val entity =
            Chapter(
                id = 0, // auto-generated
                name = chapter.name,
                marinesCount = chapter.marinesCount,
            )
        em.persist(entity)
        return entity
    }

    @Transactional
    open fun update(
        id: Long,
        @Valid chapter: Chapter,
    ): Chapter {
        findById(id) // ensure exists
        val updated =
            Chapter(
                id = id,
                name = chapter.name,
                marinesCount = chapter.marinesCount,
            )
        em.merge(updated)
        return updated
    }

    @Transactional
    open fun delete(id: Long) {
        val entity = findById(id)
        em.remove(entity)
    }
}
