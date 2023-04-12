package com.alten.bookingapp.exception;

public class InvalidCheckInDateException extends RuntimeException{

    public InvalidCheckInDateException(String message) {
        super(message);
    }
}
