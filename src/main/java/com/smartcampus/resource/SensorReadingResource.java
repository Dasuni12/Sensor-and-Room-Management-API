package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

//Handles /api/v1/sensors/{sensorId}/readings

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    // The sensor ID this resource is working with
    private final String sensorId;

    //Constructor receives the sensorId from SensorResource

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // Returns all historical readings for this sensor.

    @GET
    public Response getReadings() {
        // Check sensor exists
        if (!DataStore.sensors.containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found: " + sensorId + "\"}")
                    .build();
        }

        // Get the reading history for this sensor
        // If no readings yet, return empty list
        List<SensorReading> readings = DataStore.sensorReadings.getOrDefault(
                sensorId,
                List.of()  // Empty list if no readings
        );

        return Response.ok(readings).build();
    }

    // Adds a new reading for this sensor

    @POST
    public Response addReading(SensorReading reading) {
        // Get the sensor
        Sensor sensor = DataStore.sensors.get(sensorId);

        // Sensor must exist
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found: " + sensorId + "\"}")
                    .build();
        }

        // sensors cannot receive readings
        // they're physically disconnected or being repaired
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is under MAINTENANCE and cannot accept readings."
            );
        }

        // Auto-generate ID and timestamp if not provided by client
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(java.util.UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Save the reading to history
        DataStore.sensorReadings.get(sensorId).add(reading);

        // Update the sensor's currentValue
        // This ensures the sensor always shows its latest reading
        sensor.setCurrentValue(reading.getValue());

        // Return HTTP 201 Created
        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}