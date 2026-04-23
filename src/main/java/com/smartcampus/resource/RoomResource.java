package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

// Handles all room-related operation

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)  // All responses are JSON
@Consumes(MediaType.APPLICATION_JSON)  // All requests accept JSON
public class RoomResource {

    // Returns a list of all rooms in the system

    @GET
    public Response getAllRooms() {
        // Get all rooms from DataStore and convert to a list
        List<Room> roomList = new ArrayList<>(DataStore.rooms.values());

        // Return HTTP 200 OK with the room list
        return Response.ok(roomList).build();
    }

    @POST
    public Response createRoom(Room room) {
        // Check if a room with this ID already exists
        if (DataStore.rooms.containsKey(room.getId())) {
            // Return HTTP 409 Conflict
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Room with ID '" + room.getId() + "' already exists\"}")
                    .build();
        }

        // Add the new room to the DataStore
        DataStore.rooms.put(room.getId(), room);

        // Return HTTP 201 Created with the created room
        return Response.status(Response.Status.CREATED)
                .entity(room)
                .build();
    }

    //Gets a specific room by ID.

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        // Look up the room in DataStore
        Room room = DataStore.rooms.get(roomId);

        // If not found, return 404
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room not found: " + roomId + "\"}")
                    .build();
        }

        // Found! Return HTTP 200 OK with the room
        return Response.ok(room).build();
    }

    //Deletes a room, but ONLY if it has no sensors.
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);

        // Room doesn't exist
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room not found: " + roomId + "\"}")
                    .build();
        }

        //  Check if room has sensors
        if (!room.getSensorIds().isEmpty()) {
            // Room has sensors! Cannot delete.
            // Throw a custom exception
            throw new RoomNotEmptyException(
                    "Cannot delete room '" + roomId + "'. It still has " +
                            room.getSensorIds().size() + " sensor(s) assigned."
            );
        }

        // Safe to delete - no sensors in this room
        DataStore.rooms.remove(roomId);

        // Return HTTP 204 No Content (success, but no body to return)
        return Response.noContent().build();
    }
}