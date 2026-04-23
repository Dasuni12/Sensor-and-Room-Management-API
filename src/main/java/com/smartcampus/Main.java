package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import com.smartcampus.config.ApplicationConfig;
import java.net.URI;


public class Main {

    // The base URL where API run
    public static final String BASE_URI = "http://localhost:8080/";

    public static void main(String[] args) throws Exception {
        //  Create the JAX-RS configuration
        // ApplicationConfig tells Jersey which classes to scan for @Path annotations
        ResourceConfig config = new ApplicationConfig();

        //  Create and start the Grizzly HTTP server
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI),
                config
        );

        // Print success message
        System.out.println("===========================================");
        System.out.println("  Smart Campus API is running!");
        System.out.println("  URL: http://localhost:8080/api/v1");
        System.out.println("  Press ENTER to stop the server...");
        System.out.println("===========================================");

        // Keep server running until user presses Enter
        System.in.read();

        // Stop the server when user presses Enter
        server.stop();
        System.out.println("Server stopped.");
    }
}