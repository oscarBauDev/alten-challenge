package com.alten.bookingapp.service.impl;

import com.alten.bookingapp.exception.*;
import com.alten.bookingapp.model.dto.ModifyResponseDto;
import com.alten.bookingapp.model.dto.ReservationDTO;
import com.alten.bookingapp.model.dto.ResponseDto;
import com.alten.bookingapp.model.entities.Reservation;
import com.alten.bookingapp.model.entities.Room;
import com.alten.bookingapp.repository.ReservationRepository;
import com.alten.bookingapp.service.ReservationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Value("${reservation.max-stay-days}")
    private int MAX_STAY_DAYS;

    @Value("${reservation.max-advance-booking-days}")
    private int MAX_ADVANCE_BOOKING_DAYS;
    private static final long DEFAULT_ROOM_ID = 1L;

    private final ModelMapper mapper;

    private final ReservationRepository reservationRepository;
    private final Room room;

    public ReservationServiceImpl(ModelMapper mapper, ReservationRepository reservationRepository) {
        this.mapper = mapper;
        this.reservationRepository = reservationRepository;
        this.room = Room.builder().id(DEFAULT_ROOM_ID).build();
    }

    /**
     * Retrieves all the reservations
     *
     * @return
     */
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    /**
     * Creates a new reservation.
     *
     * @param requestDto the reservation request DTO
     * @return the newly created reservation
     * @throws InvalidCheckInDateException if the check-in date is not valid
     * @throws IllegalArgumentException    if the check-out date is not valid
     * @throws RoomNotAvailableException   if the room is not available
     */
    @Override
    public ReservationDTO placeReservation(ReservationDTO requestDto) {
        LocalDate checkIn = requestDto.getCheckInDate();
        LocalDate checkOut = requestDto.getCheckOutDate();
        requestDto.setCancelled(false);
        validateReservation(checkIn, checkOut);
        Reservation reservation = mapper.map(requestDto, Reservation.class);
        reservation.setRoom(room);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setModifiedAt(LocalDateTime.now());
        reservation = reservationRepository.save(reservation);
        return mapper.map(reservation, ReservationDTO.class);
    }

    /**
     * Modifies an existing reservation with the given ID, using the reservation request DTO.
     *
     * @param id         The ID of the reservation to modify.
     * @param requestDto The reservation request DTO.
     * @return The modified reservation.
     * @throws InvalidReservationException If the range of dates provided is the same as the existing reservation.
     * @throws InvalidCheckInDateException If the check-in date is not valid.
     * @throws IllegalArgumentException    If the check-out date is not valid.
     * @throws RoomNotAvailableException   If the room is not available.
     */

    @Override
    public ReservationDTO modifyReservation(Long id, ReservationDTO requestDto) {
        LocalDate checkIn = requestDto.getCheckInDate();
        LocalDate checkOut = requestDto.getCheckOutDate();
        String guestName = requestDto.getGuestName();
        LocalDateTime modifiedAt = LocalDateTime.now();
        Reservation reservationInDb = reservationRepository.findById(id).orElseThrow(() -> new RoomNotAvailableException("Room with id " + id + " not found."));
        validateIsReservationIsCancelled(reservationInDb);
        validateSameRangeDates(reservationInDb, checkIn, checkOut);
        validateExistingReservation(checkIn, checkOut, reservationInDb.getId());

        reservationInDb.setCheckInDate(checkIn);
        reservationInDb.setCheckOutDate(checkOut);
        reservationInDb.setGuestName(guestName);
        reservationInDb.setModifiedAt(modifiedAt);
        reservationInDb = reservationRepository.save(reservationInDb);

        return mapper.map(reservationInDb, ReservationDTO.class);
    }

    /**
     * Cancels a reservation with the specified ID.
     * If the reservation is already cancelled, throws a ReservationAlreadyCancelledException.
     * If no reservation is found with the given ID, throws a RoomNotAvailableException.
     *
     * @param id the ID of the reservation to cancel
     * @return a ModifyResponseDto containing a success message
     * @throws ReservationAlreadyCancelledException if the reservation was already cancelled
     * @throws RoomNotAvailableException            if no reservation was found with the given ID
     */
    @Override
    public ModifyResponseDto cancelReservation(Long id) {
        LocalDateTime modifiedAt = LocalDateTime.now();
        Reservation reservationInDb = reservationRepository.findById(id).orElseThrow(() -> new RoomNotAvailableException("Room with id " + id + " not found."));
        if (reservationInDb.getCancelled()) {
            throw new ReservationAlreadyCancelledException("Reservation with id " + id + " was already cancelled");
        }
        reservationInDb.setCancelled(true);
        reservationInDb.setModifiedAt(modifiedAt);
        reservationRepository.save(reservationInDb);
        return ModifyResponseDto.builder()
                .message("Reservation cancelled successfully.")
                .build();
    }

    /**
     * Checks the availability of a room for a given check-in and check-out date range.
     *
     * @param checkIn  The check-in date of the reservation.
     * @param checkOut The check-out date of the reservation.
     * @return A ResponseDto object containing information about the room's availability for the specified dates
     * @throws InvalidReservationException If the check-in date is after the check-out date or if the reservation dates are invalid.
     */
    @Override
    public ResponseDto checkRoomAvailability(LocalDate checkIn, LocalDate checkOut) {
        validateReservation(checkIn, checkOut);
        return ResponseDto.builder()
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomAvailable(true)
                .message("The room is available for these dates")
                .build();
    }

    /**
     * Validates the reservation by checking if the check-in and check-out dates are valid and available for the room.
     *
     * @param checkIn  the check-in date of the reservation
     * @param checkOut the check-out date of the reservation
     * @throws InvalidCheckInDateException  if the check-in date is invalid
     * @throws InvalidCheckOutDateException if the check-out date is invalid
     * @throws InvalidReservationException  if the reservation is invalid for any reason
     * @throws RoomNotAvailableException    if the room is not available for the reservation dates
     */
    private void validateReservation(LocalDate checkIn, LocalDate checkOut) {
        validateCheckInDate(checkIn);
        validateCheckOutDate(checkIn, checkOut);
        validateMaxStayDays(checkIn, checkOut);
        validateRoomAvailability(checkIn, checkOut);
    }

    /**
     * Validates that a reservation with the given check-in and check-out dates and reservation ID does not violate any business rules, including check-in and check-out date validity, maximum stay duration, and existing reservation conflicts for the room.
     *
     * @param checkIn       the check-in date of the reservation to be validated
     * @param checkOut      the check-out date of the reservation to be validated
     * @param reservationId the ID of the reservation to be validated, if it exists
     * @throws InvalidCheckInDateException  if the check-in date is invalid (i.e., before the current date plus one day or more than the maximum advance booking days in the future)
     * @throws InvalidCheckOutDateException if the check-out date is invalid (i.e., before the check-in date)
     * @throws InvalidReservationException  if the stay duration is longer than the maximum stay days or if the range of dates is the same as an existing reservation
     * @throws RoomNotAvailableException    if there is already an existing reservation for the given room during the specified check-in and check-out dates, except for the reservation with the given ID (if it exists)
     */
    private void validateExistingReservation(LocalDate checkIn, LocalDate checkOut, Long reservationId) {
        validateCheckInDate(checkIn);
        validateCheckOutDate(checkIn, checkOut);
        validateMaxStayDays(checkIn, checkOut);
        validateRoomAvailabilityExistingReservation(checkIn, checkOut, reservationId);
    }

    private void validateIsReservationIsCancelled(Reservation reservation) {
        boolean isReservationCancelled = reservation.getCancelled();
        Long id = reservation.getId();
        if (isReservationCancelled) {
            throw new ReservationAlreadyCancelledException("Reservation with id " + id + " was already cancelled. You can't modify it!");
        }
    }

    /**
     * Validates if the provided reservation has the same check-in and check-out dates as the given dates.
     * If the dates are the same, throws an InvalidReservationException with a message indicating that a different range of dates should be provided.
     *
     * @param reservation the reservation to check against
     * @param checkIn     the proposed check-in date
     * @param checkOut    the proposed check-out date
     * @throws InvalidReservationException if the range of dates is the same as the provided reservation
     */
    private void validateSameRangeDates(Reservation reservation, LocalDate checkIn, LocalDate checkOut) {
        if (reservation.getCheckInDate().equals(checkIn) && reservation.getCheckOutDate().equals(checkOut)) {
            throw new InvalidReservationException("The range of dates is the same. You have to provided a different range of dates");
        }

    }

    /**
     * Validates that the check-in date is within the acceptable range of dates.
     *
     * @param checkIn The check-in date to be validated.
     * @throws InvalidCheckInDateException if the check-in date is before the minimum check-in date or after the maximum check-in date.
     */
    private void validateCheckInDate(LocalDate checkIn) {
        LocalDate today = LocalDate.now();
        LocalDate minCheckIn = today.plusDays(1);
        LocalDate maxCheckIn = today.plusDays(MAX_ADVANCE_BOOKING_DAYS);

        if (checkIn.isBefore(minCheckIn)) {
            throw new InvalidCheckInDateException("Check-in date should be a date after " + minCheckIn);
        }
        if (checkIn.isAfter(maxCheckIn)) {
            throw new InvalidCheckInDateException("Check-in date should be within the next " + MAX_ADVANCE_BOOKING_DAYS + " days");
        }
    }

    /**
     * Validates if the check-out date is after the check-in date.
     *
     * @param checkIn  The check-in date of the reservation
     * @param checkOut The check-out date of the reservation
     * @throws InvalidCheckOutDateException If the check-out date is before the check-in date
     */
    private void validateCheckOutDate(LocalDate checkIn, LocalDate checkOut) {
        if (checkOut.isBefore(checkIn)) {
            throw new InvalidCheckOutDateException("Check-out date should be a date after check-in date");
        }
    }

    /**
     * Validates if the stay duration between the check-in and check-out dates is not longer than the maximum allowed stay days.
     *
     * @param checkIn  The check-in date of the reservation.
     * @param checkOut The check-out date of the reservation.
     * @throws InvalidReservationException if the stay duration is longer than the maximum allowed stay days.
     */
    private void validateMaxStayDays(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.plusDays(MAX_STAY_DAYS - 1).isBefore(checkOut)) {
            throw new InvalidReservationException("Stay cannot be longer than " + MAX_STAY_DAYS + " days");
        }
    }

    /**
     * Validates the availability of the room for the given check-in and check-out dates.
     *
     * @param checkIn  the check-in date.
     * @param checkOut the check-out date.
     * @throws RoomNotAvailableException if the room is not available for the given dates.
     */
    private void validateRoomAvailability(LocalDate checkIn, LocalDate checkOut) {
        if (!isRoomAvailable(checkIn, checkOut)) {
            throw new RoomNotAvailableException("The room is already booked for this dates");
        }
    }

    /**
     * Checks if the room is available for the given dates by querying the database for existing reservations.
     * If a reservation already exists for the room and the dates overlap with the given check-in and check-out dates,
     * a RoomNotAvailableException will be thrown.
     *
     * @param checkIn       the check-in date
     * @param checkOut      the check-out date
     * @param reservationId the ID of the current reservation (to be excluded from the check)
     * @throws RoomNotAvailableException if the room is not available for the given dates
     */
    private void validateRoomAvailabilityExistingReservation(LocalDate checkIn, LocalDate checkOut, Long reservationId) {
        List<Reservation> reservations = reservationRepository.findByRoomIdAndDatesOverlap(room, checkIn, checkOut, reservationId);
        if (!reservations.isEmpty()) {
            reservations.forEach(System.out::println);
            throw new RoomNotAvailableException("The room is already booked for this dates");
        }
    }

    /**
     * Checks if the room is available for the given dates.
     *
     * @param checkIn  the check-in date
     * @param checkOut the check-out date
     * @return true if the room is available
     */
    private boolean isRoomAvailable(LocalDate checkIn, LocalDate checkOut) {
        int numOfReservations = reservationRepository.countByRoomIdAndDatesOverlap(room, checkIn, checkOut);
        return numOfReservations == 0;
    }
}
