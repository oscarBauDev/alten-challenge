package com.alten.bookingapp.controller;

import com.alten.bookingapp.exception.*;
import com.alten.bookingapp.model.dto.ModifyResponseDto;
import com.alten.bookingapp.model.dto.ReservationDTO;
import com.alten.bookingapp.model.dto.ResponseDto;
import com.alten.bookingapp.service.impl.ReservationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    private static final String BOOKINGS_API_PATH = "/api/v1/bookings";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ReservationServiceImpl service;
    private LocalDate checkIn;
    private LocalDate checkOut;

    @BeforeEach
    public void setUp() {
        checkIn = LocalDate.now().plusDays(10L);
        checkOut = checkIn.plusDays(2L);
    }

    @Test
    void shouldCreateNewReservation() throws Exception {
        ReservationDTO reservationDto = ReservationDTO.builder()
                .guestName("Oscar Abril")
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .build();

        when(service.placeReservation(reservationDto)).thenReturn(reservationDto);

        mockMvc.perform(post("/api/v1/bookings/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.checkInDate", equalTo(checkIn.toString())))
                .andExpect(jsonPath("$.checkOutDate", equalTo(checkOut.toString())));
    }

    @Test
    void shouldReturnAvailability() throws Exception {
        LocalDate checkInDate = checkIn;
        LocalDate checkOutDate = checkOut;

        ResponseDto responseDto = ResponseDto.builder()
                .roomAvailable(true)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .message("The room is available for these dates")
                .build();

        given(service.checkRoomAvailability(checkInDate, checkOutDate)).willReturn(responseDto);

        mockMvc.perform(get(BOOKINGS_API_PATH + "/availability")
                        .param("checkIn", String.valueOf(checkInDate))
                        .param("checkOut", String.valueOf(checkOutDate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomAvailable", is(true)))
                .andExpect(jsonPath("$.checkInDate", is(checkInDate.toString())))
                .andExpect(jsonPath("$.checkOutDate", is(checkOut.toString())))
                .andExpect(jsonPath("$.message", is("The room is available for these dates")));
    }

    @Test
    void shouldCancelReservation() throws Exception {
        Long id = 3L;
        ModifyResponseDto modifyResponseDto = ModifyResponseDto.builder()
                .message("Reservation cancelled successfully.")
                .build();

        given(service.cancelReservation(id)).willReturn(modifyResponseDto);

        mockMvc.perform(delete(BOOKINGS_API_PATH + "/cancel/{reservationId}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Reservation cancelled successfully.")));
    }

    @Test
    void shouldModifyReservation() throws Exception {
        Long id = 1L;

        ReservationDTO newDTO = ReservationDTO.builder()
                .guestName("Henry Miller")
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .build();


        given(service.modifyReservation(id, newDTO)).willReturn(newDTO);

        mockMvc.perform(put(BOOKINGS_API_PATH + "/modify/{reservationId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestName", is("Henry Miller")))
                .andExpect(jsonPath("$.checkInDate", equalTo(checkIn.toString())))
                .andExpect(jsonPath("$.checkOutDate", equalTo(checkOut.toString())));
    }

    @Test
    void invalidDatesShouldReturnAndError() throws Exception {
        mockMvc.perform(post("/api/v1/bookings/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReservationDTO.builder().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations", IsNot.not(IsNull.nullValue())));
    }

    @Test
    void notAvailableRoomReturnAndError() throws Exception {
        ReservationDTO reservationDto = ReservationDTO.builder()
                .guestName("Oscar Abril")
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(2))
                .build();

        when(service.placeReservation(reservationDto)).thenThrow(new RoomNotAvailableException("The room is already booked for this dates"));

        mockMvc.perform(post("/api/v1/bookings/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The room is already booked for this dates"));
    }

    @Test
    void invalidCheckInDateReturnAndError() throws Exception {
        ReservationDTO reservationDto = ReservationDTO.builder()
                .guestName("Oscar Abril")
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(2))
                .build();

        LocalDate afterDay = LocalDate.now().plusDays(1);

        when(service.placeReservation(reservationDto)).thenThrow(new InvalidCheckInDateException("Check-in date should be a date after " + afterDay));

        mockMvc.perform(post("/api/v1/bookings/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Check-in date should be a date after " + afterDay));
    }

    @Test
    void invalidCheckOutDateReturnAndError() throws Exception {

        ReservationDTO reservationDto = ReservationDTO.builder()
                .guestName("Oscar Abril")
                .checkInDate(LocalDate.now().plusDays(5))
                .checkOutDate(LocalDate.now().plusDays(4))
                .build();

        when(service.placeReservation(reservationDto)).thenThrow(new InvalidCheckOutDateException("Check-out date should be a date after check-in date"));

        mockMvc.perform(post("/api/v1/bookings/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Check-out date should be a date after check-in date"));
    }

    @Test
    void nullDtoShouldReturnAndError() throws Exception {
        ReservationDTO reservationDto = ReservationDTO.builder()
                .build();

        when(service.placeReservation(reservationDto)).thenThrow(new RoomNotAvailableException("The room is already booked for this dates"));

        mockMvc.perform(post("/api/v1/bookings/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations", IsNot.not(IsNull.nullValue())));
    }

    @Test
    void reservationWithPLusThirtyDaysInAdvanceReturnAndError() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(31);
        LocalDate checkOut = checkIn.plusDays(2);

        ReservationDTO reservationDto = ReservationDTO.builder()
                .guestName("Oscar Abril")
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .build();

        when(service.placeReservation(reservationDto)).thenThrow(new InvalidReservationException("Stay cannot be longer than 30 days"));

        mockMvc.perform(post("/api/v1/bookings/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Stay cannot be longer than 30 days"));
    }

    @Test
    void cancelAlreadyCancelledReservationReturnError() throws Exception {
        Long id = 1L;

        ReservationDTO newDTO = ReservationDTO.builder()
                .guestName("Henry Miller")
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .build();

        given(service.modifyReservation(id, newDTO)).willThrow(new ReservationAlreadyCancelledException("Reservation with id 1 was already cancelled"));

        mockMvc.perform(put(BOOKINGS_API_PATH + "/modify/{reservationId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Reservation with id 1 was already cancelled")));
    }
}