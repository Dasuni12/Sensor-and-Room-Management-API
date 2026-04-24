package com.smartcampus.exception;

//Thrown when a resource references another resource that doesn't exist.

public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}