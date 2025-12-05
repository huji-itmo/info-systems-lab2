package com.example.fileService.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "import_history")
class ImportHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val fileName: String,

    @Column(nullable = true)
    val minioObjectName: String? = null,

    @Column(nullable = true)
    val totalRecords: Int? = null,

    @Column(nullable = false)
    val successfulCount: Int = 0,

    @Column(nullable = false)
    val failedCount: Int = 0,

    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val status: ImportStatus,

    @Column(columnDefinition = "TEXT")
    val errorMessage: String? = null,

    @Column(nullable = false)
    val contentType: String? = null
) {
    enum class ImportStatus {
        SUCCESS,          // All records imported
        PARTIAL_SUCCESS,  // Some records failed
        FAILURE           // File-level failure (parsing/validation)
    }
}