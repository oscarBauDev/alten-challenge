package com.alten.bookingapp.exception;

public class InvalidCheckOutDateException extends RuntimeException{
    public InvalidCheckOutDateException(String message) {
        super(message);
    }
}
