package com.example.fileService.beans

import io.minio.*
import io.minio.errors.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.io.InputStream
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*

@Component
@Scope(scopeName = "application", proxyMode = ScopedProxyMode.TARGET_CLASS)
class MinIOBean(private val minioClient: MinioClient) {

    private val bucketName = "uploads"
    private val logger = LoggerFactory.getLogger(MinIOBean::class.java)

    init {
        createBucketIfNotExists()
    }

    private fun createBucketIfNotExists() {
        try {
            val bucketExistsArgs = BucketExistsArgs.builder()
                .bucket(bucketName)
                .build()

            if (!minioClient.bucketExists(bucketExistsArgs)) {
                val makeBucketArgs = MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build()

                minioClient.makeBucket(makeBucketArgs)
                logger.info("Bucket '$bucketName' created successfully")
            } else {
                logger.info("Bucket '$bucketName' already exists")
            }
        } catch (e: Exception) {
            logger.error("Failed to create bucket '$bucketName'", e)
            throw RuntimeException("Bucket initialization failed", e)
        }
    }

    fun uploadFile(file: MultipartFile): String {
        val objectName = generateObjectName(file.originalFilename)
        return uploadFile(file.inputStream, objectName, file.size, file.contentType ?: "application/octet-stream")
    }

    fun uploadFile(
        inputStream: InputStream,
        objectName: String,
        size: Long,
        contentType: String
    ): String {
        inputStream.use { stream ->
            try {
                val partSize = 64 * 1024 * 1024L // 64MB - optimal for most scenarios
                val putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .stream(stream, size, partSize)
                    .contentType(contentType)
                    .build()

                minioClient.putObject(putObjectArgs)
                logger.info("File uploaded successfully: $objectName")
                return objectName
            } catch (e: Exception) {
                logger.error("Failed to upload file $objectName", e)
                throw RuntimeException("File upload failed", e)
            }
        }
    }

    private fun generateObjectName(originalFilename: String?): String {
        val safeFilename = originalFilename
            ?.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            ?: "unnamed_file"

        return "${UUID.randomUUID()}_$safeFilename"
    }
}