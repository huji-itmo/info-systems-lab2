import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.common.serialization.Deserializer

class JacksonDeserializer<T>(private val targetType: Class<T?>?) : Deserializer<T?> {
    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    override fun configure(configs: MutableMap<String?, *>?, isKey: Boolean) {
        // Optional: configure based on configs if needed
    }

    override fun deserialize(topic: String?, data: ByteArray?): T? {
        if (data == null) {
            return null
        }
        try {
            return objectMapper.readValue<T?>(data, targetType)
        } catch (e: Exception) {
            throw RuntimeException("Error deserializing JSON to $targetType", e)
        }
    }

    override fun close() {
        // Cleanup if needed
    }
}