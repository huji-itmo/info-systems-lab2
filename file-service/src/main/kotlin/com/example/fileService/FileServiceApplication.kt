package com.example.fileService

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
object FileServiceApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        SpringApplication.run(FileServiceApplication::class.java, *args)
    }
}