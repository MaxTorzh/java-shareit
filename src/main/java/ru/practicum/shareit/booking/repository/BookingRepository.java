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

    List<Booking> findByBookerId(Long bookerId);

    Page<Booking> findByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findByItemOwnerId(Long ownerId);

    Page<Booking> findByItemOwnerId(Long ownerId, Pageable pageable);

    List<Booking> findByItemIdAndStatus(Long itemId, BookingStatus status);

    List<Booking> findByBookerIdAndItemIdAndStatus(Long bookerId, Long itemId, BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :currentTime " +
            "ORDER BY b.end DESC")
    Optional<Booking> findLastBooking(@Param("itemId") Long itemId,
                                      @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > :currentTime " +
            "ORDER BY b.start ASC")
    Optional<Booking> findNextBooking(@Param("itemId") Long itemId,
                                      @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId " +
            "AND b.item.id = :itemId AND b.status = 'APPROVED' " +
            "AND b.end < :currentTime")
    List<Booking> findFinishedBookingsByUserAndItem(
            @Param("userId") Long userId,
            @Param("itemId") Long itemId,
            @Param("currentTime") LocalDateTime currentTime
    );
}
