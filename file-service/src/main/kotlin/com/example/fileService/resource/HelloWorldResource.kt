package com.example.fileService.resource


import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
class HelloWorldResource {

//    private val logger = LoggerFactory.getLogger(HelloWorldResource::class.java)

    @GetMapping("/api/space-marines/export/hi")
    fun index(): String {
        return "Greetings from Spring Boot!"
    }
}