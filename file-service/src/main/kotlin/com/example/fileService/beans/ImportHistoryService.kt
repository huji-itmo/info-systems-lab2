package com.example.fileService.beans

import com.example.fileService.model.ImportHistory
import com.example.fileService.repositories.ImportHistoryRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class ImportHistoryService(
    private val historyRepository: ImportHistoryRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(ImportHistoryService::class.java)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveHistory(history: ImportHistory) {
        try {
            historyRepository.save(history)
        } catch (e: Exception) {
            logger.error("Failed to save import history for file: ${history.fileName}", e)
        }
    }
}