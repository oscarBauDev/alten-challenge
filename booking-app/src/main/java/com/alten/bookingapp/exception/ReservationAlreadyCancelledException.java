package com.alten.bookingapp.exception;

public class ReservationAlreadyCancelledException extends RuntimeException{
    public ReservationAlreadyCancelledException(String message) {
        super(message);
    }
}
