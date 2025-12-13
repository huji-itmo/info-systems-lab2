package org.example.resources

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/hello")
open class HelloResource {
    constructor()

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    open fun sayHello(): String = "Hello from Jakarta JAX-RS with Kotlin!"
}
