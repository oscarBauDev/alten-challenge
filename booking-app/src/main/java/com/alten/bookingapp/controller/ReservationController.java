package com.alten.bookingapp.controller;

import com.alten.bookingapp.model.dto.ModifyResponseDto;
import com.alten.bookingapp.model.dto.ReservationDTO;
import com.alten.bookingapp.model.dto.ResponseDto;
import com.alten.bookingapp.model.entities.Reservation;
import com.alten.bookingapp.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1/bookings")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Reservation> getAll() {
        return service.getAllReservations();
    }

    @PostMapping("/add")
    public ResponseEntity<ReservationDTO> book(@Valid @RequestBody ReservationDTO requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.placeReservation(requestDto));
    }

    @GetMapping("/availability")
    public ResponseEntity<ResponseDto> checkAvailability(@RequestParam("checkIn") LocalDate checkIn,
                                                         @RequestParam("checkOut") LocalDate checkOut) {
        return ResponseEntity.ok(service.checkRoomAvailability(checkIn, checkOut));
    }

    @DeleteMapping("/cancel/{reservationId}")
    @ResponseStatus(HttpStatus.OK)
    public ModifyResponseDto cancelReservation(@PathVariable Long reservationId) {
        return service.cancelReservation(reservationId);
    }

    @PutMapping("/modify/{reservationId}")
    public ResponseEntity<ReservationDTO> changeReservation(@PathVariable Long reservationId,
                                                            @Valid @RequestBody ReservationDTO requestDto) {
        return new ResponseEntity<>(service.modifyReservation(reservationId, requestDto), HttpStatus.OK);
    }
}
