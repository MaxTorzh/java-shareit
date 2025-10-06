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

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = :status")
    List<Booking> findByItemIdAndStatus(
            @Param("itemId") Long itemId,
            @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = :status " +
            "AND b.end < :currentTime " +
            "ORDER BY b.end DESC ")
    Optional<Booking> findLastBooking(
            @Param("itemId") Long itemId,
            @Param("currentTime") LocalDateTime currentTime,
            @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.start > :currentTime " +
            "AND b.status = :status " +
            "ORDER BY b.start ASC")
    Optional<Booking> findNextBooking(
            @Param("itemId") Long itemId,
            @Param("currentTime") LocalDateTime currentTime,
            @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND b.item.id = :itemId " +
            "AND b.status = :status")
    List<Booking> findByBookerIdAndItemIdAndStatus(
            @Param("bookerId") Long bookerId,
            @Param("itemId") Long itemId,
            @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND b.item.id = :itemId " +
            "AND b.status = :status " +
            "AND b.end < :currentTime")
    List<Booking> findFinishedBookingsByUserAndItem(
            @Param("bookerId") Long bookerId,
            @Param("itemId") Long itemId,
            @Param("status") BookingStatus status,
            @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "ORDER BY b.start DESC")
    Page<Booking> findByBookerId(
            @Param("bookerId") Long bookerId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC")
    Page<Booking> findByBookerIdAndStatus(
            @Param("bookerId") Long bookerId,
            @Param("status") BookingStatus status,
            Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND b.start <= :currentTime " +
            "AND b.end >= :currentTime " +
            "ORDER BY b.start DESC")
    Page<Booking> findCurrentBookingsByBookerId(
            @Param("bookerId") Long bookerId,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND b.end < :currentTime " +
            "ORDER BY b.start DESC")
    Page<Booking> findPastBookingsByBookerId(
            @Param("bookerId") Long bookerId,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND b.start > :currentTime " +
            "ORDER BY b.start DESC")
    Page<Booking> findFutureBookingsByBookerId(
            @Param("bookerId") Long bookerId,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.item i " +
            "WHERE i.owner.id = :ownerId " +
            "ORDER BY b.start DESC")
    Page<Booking> findByItemOwnerId(
            @Param("ownerId") Long ownerId,
            Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC")
    Page<Booking> findByItemOwnerIdAndStatus(
            @Param("ownerId") Long ownerId,
            @Param("status") BookingStatus status,
            Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.start <= :currentTime " +
            "AND b.end >= :currentTime " +
            "ORDER BY b.start DESC")
    Page<Booking> findCurrentBookingsByItemOwnerId(
            @Param("ownerId") Long ownerId,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.end < :currentTime " +
            "ORDER BY b.start DESC")
    Page<Booking> findPastBookingsByItemOwnerId(
            @Param("ownerId") Long ownerId,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.start > :currentTime " +
            "ORDER BY b.start DESC")
    Page<Booking> findFutureBookingsByItemOwnerId(
            @Param("ownerId") Long ownerId,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable);
}
