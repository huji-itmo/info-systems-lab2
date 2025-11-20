import jakarta.ws.rs.ApplicationPath
import jakarta.ws.rs.core.Application
import org.example.config.ObjectMapperResolver

@ApplicationPath("/api")
class JaxRsApplication : Application()
