package com.example.fileService.beans

import io.minio.MinioClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.annotation.ApplicationScope

@Configuration
class MinIOConfig {

    @Bean
    @ApplicationScope
    fun minioClient(): MinioClient {
        val minioUrl = System.getenv("MINIO_URL")
            ?: throw IllegalStateException("MINIO_URL environment variable is required")

        val accessKey = System.getenv("MINIO_ROOT_USER")
            ?: throw IllegalStateException("MINIO_ROOT_USER environment variable is required")

        val secretKey = System.getenv("MINIO_SECRET_KEY")
            ?: throw IllegalStateException("MINIO_SECRET_KEY environment variable is required")

        return MinioClient.builder()
            .endpoint(minioUrl)
            .credentials(accessKey, secretKey)
            .build()
    }
}