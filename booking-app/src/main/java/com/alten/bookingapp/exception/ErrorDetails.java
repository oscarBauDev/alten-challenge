package com.alten.bookingapp.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class ErrorDetails {

    private String errorCode;
    private String details;
    private String message;

}
