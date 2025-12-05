package com.example.fileService.beans

import com.example.fileService.model.dto.SpaceMarineImportRequest
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JacksonJsonSerializer

@EnableKafka
@Configuration
class KafkaProducerConfig {
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String // Inject from properties

    @Bean
    fun producerFactory(): ProducerFactory<String, List<SpaceMarineImportRequest>> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JacksonJsonSerializer::class.java
        val serializer = JacksonJsonSerializer<List<SpaceMarineImportRequest>>()

        return DefaultKafkaProducerFactory(
            configProps,
            StringSerializer(),
            serializer,
        )
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, List<SpaceMarineImportRequest>> = KafkaTemplate(producerFactory())
}
