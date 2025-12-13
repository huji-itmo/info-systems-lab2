package com.example.fileService.repositories

import org.example.shared.model.Chapter
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ChapterRepository : CrudRepository<Chapter, Long> {}
