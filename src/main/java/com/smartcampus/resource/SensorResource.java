package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//Handles all sensor-related operations

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    //Returns all sensors, optionally filtered by type

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        // Start with all sensors
        List<Sensor> result = new ArrayList<>(DataStore.sensors.values());

        // If type filter is provided, filter the list
        if (type != null && !type.isEmpty()) {
            // Java Streams which filter only sensors matching the type
            // equalsIgnoreCase which case-insensitive comparison for CO2 = co2 = Co2
            result = result.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        // Return the filtered (or full) list
        return Response.ok(result).build();
    }


    //Creates a new sensor

    @POST
    public Response createSensor(Sensor sensor) {
        // This prevents creating sensors for non-existent rooms
        if (!DataStore.rooms.containsKey(sensor.getRoomId())) {
            // if room doesn't exist it throws custom exception
            // the exception mapper will convert this to HTTP 422
            throw new LinkedResourceNotFoundException(
                    "Cannot create sensor. Room with ID '" + sensor.getRoomId() + "' does not exist."
            );
        }

        // check if sensor ID already exists
        if (DataStore.sensors.containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Sensor ID already exists\"}")
                    .build();
        }

        // all validations passed it Saves the sensor
        DataStore.sensors.put(sensor.getId(), sensor);

        // link this sensor to its room
        Room room = DataStore.rooms.get(sensor.getRoomId());
        room.getSensorIds().add(sensor.getId());

        // initialize empty readings list for this sensor
        DataStore.sensorReadings.put(sensor.getId(), new ArrayList<>());

        // return HTTP 201 Created
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    //Gets a specific sensor by ID

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        // Look up the sensor in DataStore
        Sensor sensor = DataStore.sensors.get(sensorId);

        // If not found, return 404
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found: " + sensorId + "\"}")
                    .build();
        }

        // Found! Return HTTP 200 OK with the sensor
        return Response.ok(sensor).build();
    }

    //sub-resource locator for sensor readings

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}