package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByBookerId(Long bookerId, Pageable pageable);

    Page<Booking> findByItemOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = :status")
    List<Booking> findByItemIdAndStatus(@Param("itemId") Long itemId,
                                        @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = :status " +
            "AND b.end < :currentTime " +
            "ORDER BY b.end DESC ")
    Optional<Booking> findLastBooking(@Param("itemId") Long itemId,
                                      @Param("currentTime") LocalDateTime currentTime,
                                      @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.start > :currentTime " +
            "AND b.status = :status " +
            "ORDER BY b.start ASC")
    Optional<Booking> findNextBooking(@Param("itemId") Long itemId,
                                      @Param("currentTime") LocalDateTime currentTime,
                                      @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.item.id = :itemId AND b.status = :status")
    List<Booking> findByBookerIdAndItemIdAndStatus(@Param("bookerId") Long bookerId,
                                                   @Param("itemId") Long itemId,
                                                   @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.item.id = :itemId " +
            "AND b.status = :status AND b.end < :currentTime")
    List<Booking> findFinishedBookingsByUserAndItem(@Param("bookerId") Long bookerId,
                                                    @Param("itemId") Long itemId,
                                                    @Param("status") BookingStatus status,
                                                    @Param("currentTime") LocalDateTime currentTime);
}
