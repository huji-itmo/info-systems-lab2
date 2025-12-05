package com.example.fileService.resource

import com.example.fileService.repositories.ImportHistoryRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/space-marines")
class ImportHistoryResource(
    private val historyRepository: ImportHistoryRepository
) {

    @GetMapping("/import-history")
    fun getImportHistory(
        @PageableDefault(
            size = 20,
            sort = ["timestamp"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ) = historyRepository.findAll(pageable)
}