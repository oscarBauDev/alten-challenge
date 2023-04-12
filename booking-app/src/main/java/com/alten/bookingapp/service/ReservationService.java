package com.alten.bookingapp.service;

import com.alten.bookingapp.model.dto.ModifyResponseDto;
import com.alten.bookingapp.model.dto.ReservationDTO;
import com.alten.bookingapp.model.dto.ResponseDto;
import com.alten.bookingapp.model.entities.Reservation;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    List<Reservation> getAllReservations();

    ReservationDTO placeReservation(ReservationDTO requestDto);

    ResponseDto checkRoomAvailability(LocalDate checkIn, LocalDate checkOut);

    ModifyResponseDto cancelReservation(Long id);

    ReservationDTO modifyReservation(Long id, ReservationDTO requestDto);
}
