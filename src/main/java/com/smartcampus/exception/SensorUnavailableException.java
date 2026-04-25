package com.smartcampus.exception;

// thrown when trying to post a reading to a sensor in maintenance status

public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}