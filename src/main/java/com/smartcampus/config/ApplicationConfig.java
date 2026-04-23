package com.smartcampus.config;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api/v1")
public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        // Tell Jersey to scan these packages for classes with JAX-RS annotations
        // It will find @Path, @GET, @POST, @Provider, etc.
        packages(
                "com.smartcampus.resource",   // Where endpoint classes live
                "com.smartcampus.mapper"       // Where exception mappers live
        );

        // Register Jackson converts Java objects to JSON automatically
        register(JacksonFeature.class);
    }
}