package com.smartcampus.exception;

//Thrown when trying to delete a room that still has sensors

public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}