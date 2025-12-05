package org.example.service

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.StringDeserializer
import org.example.model.dto.SpaceMarineImportRequest
import java.time.Duration
import java.util.Properties
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

@ApplicationScoped
open class KafkaListenerService {

    private lateinit var consumer: KafkaConsumer<String, List<SpaceMarineImportRequest>>
    private lateinit var executor: ExecutorService

    @Inject
    private lateinit var spaceMarineService: SpaceMarineService

    @Volatile
    private var running = true
    private val logger = Logger.getLogger(KafkaListenerService::class.java.name)

    @PostConstruct
    open fun init() {
        var retries = 5
        while (retries > 0) {
            try {
                logger.info("Initializing Kafka consumer (attempt ${6 - retries}/5)")
                logger.info("Initializing Kafka consumer")
                val props = Properties().apply {
                    put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
                    put(ConsumerConfig.GROUP_ID_CONFIG, "space-marine-group")
                    put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
                    put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonDeserializer::class.java.name)
                    put("json.value.type", "java.util.List<org.example.model.dto.SpaceMarineImportRequest>".trim())
                    put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
                    put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
                }

                // Initialize consumer here (no injection)
                consumer = KafkaConsumer(props)
                consumer.subscribe(listOf("uploads"))
                logger.info("Subscribed to topic 'uploads'")

                // Initialize executor here (no injection)
                logger.info("[KAFKA] Starting listener thread");
                executor = Executors.newSingleThreadExecutor()
                executor.submit {
                    logger.info("Kafka consumer thread started")
                    while (running) {
                        try {
                            val records = consumer.poll(Duration.ofMillis(500))
                            if (records.count() > 0) {
                                logger.info("Received ${records.count()} batches")
                            }

                            records.forEach { record ->
                                logger.info("Processing batch with ${record.value().size} records")
                                handleMessage(record.value())
                            }
                            consumer.commitSync() // Commit after processing all batches
                        } catch (e: WakeupException) {
                            // Expected during shutdown
                        } catch (e: Exception) {
                            logger.log(Level.SEVERE, "Consumer error", e)
                        }
                    }
                    logger.info("Kafka consumer thread stopped")
                }
                logger.info("Successfully initialized Kafka consumer")
                return
            } catch (e: Exception) {
                logger.warning("Kafka init failed: ${e.message}. Retrying in 5s...")
                retries--
                Thread.sleep(5000)
            }
        }
        throw RuntimeException("Kafka consumer initialization failed after 5 attempts")
    }

    private fun handleMessage(records: List<SpaceMarineImportRequest>) {
        records.forEach { request ->
            try {
                logger.info("Importing marine: ${request.name}")
                spaceMarineService.processImportRequest(request)
            } catch (e: Exception) {
                logger.log(Level.WARNING, "Failed to import ${request.name}", e)
            }
        }
    }

    @PreDestroy
    open fun cleanup() {
        logger.info("Shutting down Kafka consumer")
        running = false
        consumer.wakeup() // Interrupt poll()

        // Proper shutdown sequence
        executor.shutdown()
        consumer.close()

        // Wait for executor to terminate
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            executor.shutdownNow()
        }

        logger.info("Kafka resources closed")
    }
}