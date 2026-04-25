package com.smartcampus.mapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import java.util.logging.Logger;


 // global safety net which catches any exception not handled by other mappers

 // this mapper returns a generic 500 error to the client while logging the real error server-side for debugging

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Log the real error on the server side
        // this goes to the server console not to the client
        LOGGER.severe("Unexpected error occurred: " + ex.getMessage());
        LOGGER.severe("Exception type: " + ex.getClass().getName());

        // Print stack trace to server logs
        ex.printStackTrace();

        // Return a safe generic message to the client
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "error", "500 Internal Server Error",
                        "message", "An unexpected error occurred. Please contact support."
                ))
                .build();
    }
}