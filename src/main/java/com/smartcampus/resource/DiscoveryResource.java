package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;


@Path("/")
@Produces(MediaType.APPLICATION_JSON)  // Always return JSON
public class DiscoveryResource {


    @GET
    public Response discover() {
        // Create a map to hold response data
        Map<String, Object> info = new HashMap<>();

        // Basic API information
        info.put("api", "Smart Campus Sensor & Room Management API");
        info.put("version", "1.0");
        info.put("contact", "admin@smartcampus.ac.uk");

        // HATEOAS - provide links to other resources
        // This tells clients where to find rooms and sensors
        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        info.put("resources", links);

        // Return HTTP 200 OK with the JSON data
        return Response.ok(info).build();
    }
}
