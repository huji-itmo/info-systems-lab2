package com.example.fileService.beans

import com.example.fileService.model.SpaceMarineImportRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

@Component
class KafkaBean(
    private val kafkaTemplate: KafkaTemplate<String, List<SpaceMarineImportRequest>>,
) {
    fun sendMessage(message: List<SpaceMarineImportRequest>) {
        val future: CompletableFuture<SendResult<String, List<SpaceMarineImportRequest>>> =
            kafkaTemplate.send("uploads", message)

        future.whenComplete(
            BiConsumer { result: SendResult<String, List<SpaceMarineImportRequest>>, ex: Throwable? ->
                if (ex == null) {
                    println(
                        "Sent message=[" + message +
                            "] with offset=[" + result.recordMetadata.offset() + "]",
                    )
                } else {
                    println(
                        "Unable to send message=[" +
                            message + "] due to : " + ex.message,
                    )
                }
            },
        )
    }
}
