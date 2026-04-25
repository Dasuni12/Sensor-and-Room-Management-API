package com.smartcampus.mapper;

import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;


 // logs every incoming HTTP request and outgoing response
 // this filter automatically runs before and after EVERY API call

@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

     // runs BEFORE every request is processed.
     // logs the HTTP method and URI.

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Get the HTTP method
        String method = requestContext.getMethod();

        // Get the full URI
        String uri = requestContext.getUriInfo().getRequestUri().toString();

        // Log it
        LOGGER.info(String.format(">>> REQUEST: %s %s", method, uri));
    }

     // runs AFTER every response is sent
     // logs the HTTP method, URI, and final status code

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        // Get request info
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();

        // Get response status code
        int statusCode = responseContext.getStatus();

        // Log it
        LOGGER.info(String.format("<<< RESPONSE: %s %s → HTTP %d", method, uri, statusCode));
    }
}