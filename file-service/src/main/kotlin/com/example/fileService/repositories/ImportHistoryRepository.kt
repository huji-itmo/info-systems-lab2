package com.example.fileService.repositories

import com.example.fileService.model.ImportHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImportHistoryRepository : JpaRepository<ImportHistory, Long>