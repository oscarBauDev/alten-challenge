package com.alten.bookingapp.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ReservationDTO {

    @JsonIgnore
    private Long id;

    @NotNull(message = "guest-name must not be null")
    @NotBlank(message = "guest-name must not be empty or blank")
    private String guestName;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    @NotNull(message = "Check-in date must not be null")
    @FutureOrPresent(message = "Check-in date must be a future or present date")
    private LocalDate checkInDate;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    @NotNull(message = "Check-out date must not be null")
    @Future(message = "Check-out date must be a future date")
    private LocalDate checkOutDate;
    @JsonIgnore
    private Long roomId;

    @JsonIgnore
    private boolean cancelled;
}
