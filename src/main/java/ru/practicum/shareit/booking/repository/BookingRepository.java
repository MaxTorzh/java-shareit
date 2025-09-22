package ru.practicum.shareit.booking.repository;

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

    List<Booking> findByItemId(Long itemId);

    List<Booking> findByItemOwnerId(Long ownerId);

    List<Booking> findByItemIdAndStatus(Long itemId, BookingStatus status);

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

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.booker.id = :userId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :currentTime")
    Boolean existsByItemIdAndBookerIdAndEndBefore(@Param("itemId") Long itemId,
                                                  @Param("userId") Long userId,
                                                  @Param("currentTime") LocalDateTime currentTime);
}
