package com.alten.bookingapp.service.impl;

import com.alten.bookingapp.exception.*;
import com.alten.bookingapp.model.dto.ModifyResponseDto;
import com.alten.bookingapp.model.dto.ReservationDTO;
import com.alten.bookingapp.model.dto.ResponseDto;
import com.alten.bookingapp.model.entities.Reservation;
import com.alten.bookingapp.model.entities.Room;
import com.alten.bookingapp.repository.ReservationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ReservationServiceImpl.class)
class ReservationServiceImplTest {

    @MockBean
    private ReservationRepository repository;

    @MockBean
    private ModelMapper mapper;

    @MockBean
    private Room room;

    @Autowired
    private ReservationServiceImpl service;

    private LocalDate checkIn;
    private LocalDate checkOut;


    @BeforeEach
    public void setUp() {
        checkIn = LocalDate.now().plusDays(10L);
        checkOut = checkIn.plusDays(2L);
    }

    @Test
    void createWithSuccess() {
        ReservationDTO reservationDto = ReservationDTO.builder()
                .guestName("Oscar Abril")
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .cancelled(false)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .guestName(reservationDto.getGuestName())
                .checkInDate(reservationDto.getCheckInDate())
                .checkOutDate(reservationDto.getCheckOutDate())
                .cancelled(false)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .room(room)
                .build();

        when(mapper.map(reservationDto, Reservation.class)).thenReturn(reservation);
        when(repository.save(any(Reservation.class))).thenReturn(reservation);

        service.placeReservation(reservationDto);

        verify(mapper, times(1)).map(reservationDto, Reservation.class);
        verify(repository, times(1)).save(reservation);
        verify(mapper, times(1)).map(reservation, ReservationDTO.class);
    }

    @Test
    void createReservationWithCheckinTodayError() {
        ReservationDTO reservationDto = ReservationDTO.builder()
                .guestName("Oscar Abril")
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(2))
                .cancelled(false)
                .build();

        Assertions.assertThrows(InvalidCheckInDateException.class, () -> service.placeReservation(reservationDto));
    }

    @Test
    void createReservationWithCheckOutDateBeforeCheckInError() {
        ReservationDTO reservationDto = ReservationDTO.builder()
                .guestName("Oscar Abril")
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now())
                .cancelled(false)
                .build();

        Assertions.assertThrows(InvalidCheckOutDateException.class, () -> service.placeReservation(reservationDto));
    }

    @Test
    void createReservationWithCheckInDatePlusThirtyDaysError() {
        ReservationDTO reservationDto = ReservationDTO.builder()
                .guestName("Oscar Abril")
                .checkInDate(LocalDate.now().plusDays(31))
                .checkOutDate(LocalDate.now().plusDays(33))
                .cancelled(false)
                .build();

        Assertions.assertThrows(InvalidCheckInDateException.class, () -> service.placeReservation(reservationDto));
    }

    @Test
    void createReservationWithStayMoreThanThreeDaysError() {
        ReservationDTO reservationDto = ReservationDTO.builder()
                .guestName("Oscar Abril")
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(4))
                .cancelled(false)
                .build();

        Assertions.assertThrows(InvalidReservationException.class, () -> service.placeReservation(reservationDto));
    }

    @Test
    void createReservationWithRoomNotAvailableError() {
        ReservationDTO reservationDto = ReservationDTO.builder()
                .guestName("Oscar Abril")
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .cancelled(false)
                .build();

        when(repository.countByRoomIdAndDatesOverlap(any(Room.class), any(LocalDate.class), any(LocalDate.class))).thenReturn(2);

        Assertions.assertThrows(RoomNotAvailableException.class, () -> service.placeReservation(reservationDto));
    }

    @Test
    void modifyReservationWithSuccess() {
        Reservation reservationInDB = Reservation.builder()
                .id(1L)
                .guestName("Oscar Abril")
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .createdAt(LocalDateTime.now().minusDays(20))
                .modifiedAt(LocalDateTime.now().minusDays(20))
                .room(room)
                .cancelled(false)
                .build();

        ReservationDTO reservationDTO = ReservationDTO.builder()
                .guestName("Oscar Abril")
                .checkInDate(checkOut)
                .checkOutDate(checkOut.plusDays(2))
                .build();

        Reservation newReservation = Reservation.builder()
                .id(1L)
                .guestName(reservationDTO.getGuestName())
                .checkInDate(reservationDTO.getCheckInDate())
                .checkOutDate(reservationDTO.getCheckOutDate())
                .createdAt(reservationInDB.getCreatedAt())
                .modifiedAt(LocalDateTime.now())
                .room(room)
                .cancelled(false)
                .build();

        when(repository.findById(reservationInDB.getId())).thenReturn(Optional.of(reservationInDB));
        when(repository.save(newReservation)).thenReturn(newReservation);

        service.modifyReservation(reservationInDB.getId(), reservationDTO);

        verify(repository, times(1)).findById(reservationInDB.getId());
        verify(repository, times(1)).save(reservationInDB);
    }

    @Test
    void modifyReservationWithSameDatesError() {
        Reservation reservationInDB = Reservation.builder()
                .id(1L)
                .guestName("Oscar Abril")
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .createdAt(LocalDateTime.now().minusDays(20))
                .modifiedAt(LocalDateTime.now().minusDays(20))
                .room(room)
                .cancelled(false)
                .build();

        ReservationDTO reservationDTO = ReservationDTO.builder()
                .guestName("Oscar Abril")
                .checkInDate(reservationInDB.getCheckInDate())
                .checkOutDate(reservationInDB.getCheckOutDate())
                .build();

        when(repository.findById(reservationInDB.getId())).thenReturn(Optional.of(reservationInDB));

        Assertions.assertThrows(InvalidReservationException.class, () -> service.modifyReservation(reservationInDB.getId(), reservationDTO));
    }

    @Test
    void modifyReservationWithRoomAlreadyBooked() {
        Reservation reservationInDB = Reservation.builder()
                .id(1L)
                .guestName("Oscar Abril")
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .createdAt(LocalDateTime.now().minusDays(20))
                .modifiedAt(LocalDateTime.now().minusDays(20))
                .room(room)
                .cancelled(false)
                .build();

        ReservationDTO reservationDTO = ReservationDTO.builder()
                .guestName("Oscar Abril")
                .checkInDate(checkOut)
                .checkOutDate(checkOut.plusDays(2))
                .build();

        when(repository.findByRoomIdAndDatesOverlap(any(Room.class), any(LocalDate.class), any(LocalDate.class), any(Long.class))).thenReturn(List.of(reservationInDB));

        Assertions.assertThrows(RoomNotAvailableException.class, () -> service.modifyReservation(reservationInDB.getId(), reservationDTO));
    }

    @Test
    void cancelReservationWithSuccess() {
        Reservation reservationInDB = Reservation.builder()
                .id(1L)
                .guestName("Oscar Abril")
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .createdAt(LocalDateTime.now().minusDays(10))
                .modifiedAt(LocalDateTime.now().minusDays(10))
                .room(room)
                .cancelled(false)
                .build();

        reservationInDB.setModifiedAt(LocalDateTime.now());

        ModifyResponseDto expected = ModifyResponseDto.builder()
                .message("Reservation cancelled successfully.")
                .build();

        when(repository.findById(reservationInDB.getId())).thenReturn(Optional.of(reservationInDB));
        when(repository.save(reservationInDB)).thenReturn(reservationInDB);

        service.cancelReservation(reservationInDB.getId());


        verify(repository, times(1)).findById(reservationInDB.getId());
        verify(repository, times(1)).save(reservationInDB);
    }

    @Test
    void cancelReservationWithReservationAlreadyCancelled() {
        Reservation reservationInDB = Reservation.builder()
                .id(1L)
                .guestName("Oscar Abril")
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .createdAt(LocalDateTime.now().minusDays(10))
                .modifiedAt(LocalDateTime.now().minusDays(10))
                .room(room)
                .cancelled(true)
                .build();

        when(repository.findById(reservationInDB.getId())).thenReturn(Optional.of(reservationInDB));

        Assertions.assertThrows(ReservationAlreadyCancelledException.class, () -> service.cancelReservation(reservationInDB.getId()));

    }

    @Test
    void checkRoomAvailability() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);

        ResponseDto expected = ResponseDto.builder()
                .roomAvailable(true)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .message("The room is available for these dates")
                .build();


        when(repository.countByRoomIdAndDatesOverlap(room, checkIn, checkOut)).thenReturn(0);

        ResponseDto response = service.checkRoomAvailability(checkIn, checkOut);

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(repository, times(1)).countByRoomIdAndDatesOverlap(roomCaptor.capture(), eq(checkIn), eq(checkOut));
        assertEquals(expected, response);
    }
}