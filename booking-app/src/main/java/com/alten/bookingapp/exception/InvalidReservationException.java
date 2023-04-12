package com.alten.bookingapp.exception;

public class InvalidReservationException extends RuntimeException{
    public InvalidReservationException(String message) {
        super(message);
    }
}
