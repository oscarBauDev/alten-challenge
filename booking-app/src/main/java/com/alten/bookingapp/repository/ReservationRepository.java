package com.alten.bookingapp.repository;

import com.alten.bookingapp.model.entities.Reservation;
import com.alten.bookingapp.model.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    @Query("SELECT COUNT(r) " +
            "FROM Reservation r " +
            "WHERE r.room = :room " +
            "AND r.checkOutDate >= :checkInDate " +
            "AND r.checkInDate <= :checkOutDate " +
            "AND r.cancelled = false")
    int countByRoomIdAndDatesOverlap(@Param("room") Room room,
                                     @Param("checkInDate") LocalDate checkInDate,
                                     @Param("checkOutDate") LocalDate checkOutDate);

    @Query("SELECT r " +
            "FROM Reservation r " +
            "WHERE r.room = :room " +
            "AND r.checkOutDate >= :checkInDate " +
            "AND r.checkInDate <= :checkOutDate " +
            "AND r.cancelled = false " +
            "AND r.id <> :reservationId")
    List<Reservation> findByRoomIdAndDatesOverlap(@Param("room") Room room,
                                                  @Param("checkInDate") LocalDate checkInDate,
                                                  @Param("checkOutDate") LocalDate checkOutDate,
                                                  @Param("reservationId") Long reservationId);


    @Query("SELECT r " +
            "FROM Reservation r " +
            "WHERE r.room != :room " +
            "AND r.checkOutDate >= :checkInDate " +
            "AND r.checkInDate <= :checkOutDate " +
            "AND r.cancelled = false ")
    List<Reservation> findConflictingReservations(@Param("room") Room room,
                                                  @Param("checkInDate") LocalDate checkInDate,
                                                  @Param("checkOutDate") LocalDate checkOutDate);
}
