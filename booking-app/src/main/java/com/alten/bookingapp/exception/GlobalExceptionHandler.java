package com.alten.bookingapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {InvalidCheckInDateException.class})
    public ResponseEntity<ErrorDetails> handleInvalidCheckInDateException(InvalidCheckInDateException ex,
                                                                          WebRequest request) {
        ErrorDetails errorResponse = new ErrorDetails(HttpStatus.BAD_REQUEST.toString(),
                request.getDescription(false),
                ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(value = {InvalidCheckOutDateException.class})
    public ResponseEntity<ErrorDetails> handleInvalidCheckOutDateException(InvalidCheckOutDateException ex,
                                                                           WebRequest request) {
        ErrorDetails errorResponse = new ErrorDetails(HttpStatus.BAD_REQUEST.toString(),
                request.getDescription(false),
                ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(value = {InvalidReservationException.class})
    public ResponseEntity<ErrorDetails> handleInvalidReservationException(InvalidReservationException ex,
                                                                          WebRequest request) {
        ErrorDetails errorResponse = new ErrorDetails(HttpStatus.BAD_REQUEST.toString(),
                request.getDescription(false),
                ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(RoomNotAvailableException.class)
    public ResponseEntity<ErrorDetails> handleRoomNotAvailableException(RoomNotAvailableException ex,
                                                                        WebRequest request) {
        ErrorDetails errorResponse = new ErrorDetails(HttpStatus.BAD_REQUEST.toString(),
                request.getDescription(false),
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    @ExceptionHandler(ReservationAlreadyCancelledException.class)
    public ResponseEntity<ErrorDetails> handleReservationAlreadyCancelledException(ReservationAlreadyCancelledException ex, WebRequest request) {
        ErrorDetails errorResponse = new ErrorDetails(HttpStatus.BAD_REQUEST.toString(),
                request.getDescription(false),
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                        WebRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        ValidationError validationError = new ValidationError();
        for (FieldError fieldError : fieldErrors) {
            ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST.toString(),
                    fieldError.getField(),
                    fieldError.getDefaultMessage());
            validationError.addViolations(errorDetails);
        }

        return ResponseEntity.badRequest().body(validationError);
    }
}
