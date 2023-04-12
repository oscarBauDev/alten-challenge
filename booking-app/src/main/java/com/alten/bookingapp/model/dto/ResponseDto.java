package com.alten.bookingapp.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
public class ResponseDto {
    private boolean roomAvailable;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private LocalDate checkInDate;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private LocalDate checkOutDate;
    private String message;
}

