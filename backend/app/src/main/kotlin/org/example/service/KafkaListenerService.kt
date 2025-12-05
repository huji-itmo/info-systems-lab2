package org.example.service

import com.fasterxml.jackson.databind.JsonDeserializer
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.ejb.Singleton
import jakarta.ejb.Startup
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.example.model.dto.SpaceMarineImportRequest
import org.example.resources.CoordinatesResource
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.logging.Logger

@ApplicationScoped
class KafkaListenerService {

    private lateinit var consumer: KafkaConsumer<String, List<SpaceMarineImportRequest>>
    private lateinit var executor: ExecutorService

    @Inject
    private lateinit var spaceMarineService: SpaceMarineService

    private val logger = Logger.getLogger(KafkaListenerService::class.java.name)

    @PostConstruct
    fun init() {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "kafka:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "payara-group",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JacksonDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )

        consumer = KafkaConsumer(props)
        consumer.subscribe(listOf("uploads"))

        executor = Executors.newSingleThreadExecutor()
        executor.submit {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val records: ConsumerRecords<String, List<SpaceMarineImportRequest>> =
                        consumer.poll(Duration.ofMillis(100))
                    for (record in records) {
                        handleMessage(record.key(), record.value())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun handleMessage(key: String?, value: List<SpaceMarineImportRequest>) {
        logger.info("Received from Kafka - Key: $key, Value: $value")
        value.forEach { request -> spaceMarineService.processImportRequest(request) }
    }

    @PreDestroy
    fun cleanup() {
        executor.shutdownNow()
        consumer.close()
    }
}