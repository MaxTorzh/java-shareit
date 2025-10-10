package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingListDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookingMapperImplTest {

    @Autowired
    private BookingMapper bookingMapper;

    private Booking booking;
    private BookItemRequestDto bookingRequestDto;
    private Item item;
    private User booker;

    @BeforeEach
    void setUp() {
        booker = new User();
        booker.setId(1L);
        booker.setName("Test Booker");
        booker.setEmail("booker@test.com");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        bookingRequestDto = new BookItemRequestDto();
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));
    }

    /**
     * Тест преобразования Booking в BookingDto.
     * Проверяет успешное преобразование всех полей.
     */
    @Test
    void toDto_shouldConvertBookingToBookingDto() {
        BookingDto result = bookingMapper.toDto(booking);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(booking.getStatus().toString(), result.getStatus());

        assertNotNull(result.getBooker());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(booker.getName(), result.getBooker().getName());
        assertEquals(booker.getEmail(), result.getBooker().getEmail());

        assertNotNull(result.getItem());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(item.getName(), result.getItem().getName());
        assertEquals(item.getDescription(), result.getItem().getDescription());
        assertEquals(item.getAvailable(), result.getItem().getAvailable());
    }

    /**
     * Тест преобразования Booking в BookingDto с null Booking.
     * Проверяет обработку null значения.
     */
    @Test
    void toDto_shouldReturnNullWhenBookingIsNull() {
        BookingDto result = bookingMapper.toDto(null);

        assertNull(result);
    }

    /**
     * Тест преобразования Booking в BookingDto с null Booker.
     * Проверяет обработку null значения в поле booker.
     */
    @Test
    void toDto_shouldHandleNullBooker() {
        booking.setBooker(null);

        BookingDto result = bookingMapper.toDto(booking);

        assertNotNull(result);
        assertNull(result.getBooker());
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
    }

    /**
     * Тест преобразования Booking в BookingDto с null Item.
     * Проверяет обработку null значения в поле item.
     */
    @Test
    void toDto_shouldHandleNullItem() {
        booking.setItem(null);

        BookingDto result = bookingMapper.toDto(booking);

        assertNotNull(result);
        assertNull(result.getItem());
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
    }

    /**
     * Тест преобразования Booking в BookingDto с различными статусами.
     * Проверяет обработку разных статусов бронирования.
     */
    @Test
    void toDto_shouldHandleDifferentStatuses() {
        // Тест APPROVED статуса
        booking.setStatus(BookingStatus.APPROVED);
        BookingDto result1 = bookingMapper.toDto(booking);
        assertEquals(BookingStatus.APPROVED.toString(), result1.getStatus());

        // Тест REJECTED статуса
        booking.setStatus(BookingStatus.REJECTED);
        BookingDto result2 = bookingMapper.toDto(booking);
        assertEquals(BookingStatus.REJECTED.toString(), result2.getStatus());

        // Тест WAITING статуса
        booking.setStatus(BookingStatus.WAITING);
        BookingDto result3 = bookingMapper.toDto(booking);
        assertEquals(BookingStatus.WAITING.toString(), result3.getStatus());

        // Тест CANCELED статуса
        booking.setStatus(BookingStatus.CANCELLED);
        BookingDto result4 = bookingMapper.toDto(booking);
        assertEquals(BookingStatus.CANCELLED.toString(), result4.getStatus());
    }

    /**
     * Тест преобразования Booking в BookingListDto.
     * Проверяет успешное преобразование всех полей.
     */
    @Test
    void toListDto_shouldConvertBookingToBookingListDto() {
        BookingListDto result = bookingMapper.toListDto(booking);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(booking.getStatus().toString(), result.getStatus());
        assertEquals(item.getId(), result.getItemId());
        assertEquals(item.getName(), result.getItemName());
        assertEquals(booker.getId(), result.getBookerId());
        assertEquals(booker.getName(), result.getBookerName());
    }

    /**
     * Тест преобразования Booking в BookingListDto с null Booking.
     * Проверяет обработку null значения.
     */
    @Test
    void toListDto_shouldReturnNullWhenBookingIsNull() {
        BookingListDto result = bookingMapper.toListDto(null);

        assertNull(result);
    }

    /**
     * Тест преобразования Booking в BookingListDto с null Item.
     * Проверяет обработку null значения в поле item.
     */
    @Test
    void toListDto_shouldHandleNullItem() {
        booking.setItem(null);

        BookingListDto result = bookingMapper.toListDto(booking);

        assertNotNull(result);
        assertNull(result.getItemId());
        assertNull(result.getItemName());
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
    }

    /**
     * Тест преобразования Booking в BookingListDto с null Booker.
     * Проверяет обработку null значения в поле booker.
     */
    @Test
    void toListDto_shouldHandleNullBooker() {
        booking.setBooker(null);

        BookingListDto result = bookingMapper.toListDto(booking);

        assertNotNull(result);
        assertNull(result.getBookerId());
        assertNull(result.getBookerName());
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
    }

    /**
     * Тест преобразования Booking в BookingListDto с null Item ID.
     * Проверяет обработку null значения в поле item.id.
     */
    @Test
    void toListDto_shouldHandleNullItemId() {
        item.setId(null);
        booking.setItem(item);

        BookingListDto result = bookingMapper.toListDto(booking);

        assertNotNull(result);
        assertNull(result.getItemId());
        assertEquals(item.getName(), result.getItemName());
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
    }

    /**
     * Тест преобразования Booking в BookingListDto с null Item Name.
     * Проверяет обработку null значения в поле item.name.
     */
    @Test
    void toListDto_shouldHandleNullItemName() {
        item.setName(null);
        booking.setItem(item);

        BookingListDto result = bookingMapper.toListDto(booking);

        assertNotNull(result);
        assertNull(result.getItemName());
        assertEquals(item.getId(), result.getItemId());
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
    }

    /**
     * Тест преобразования Booking в BookingListDto с null Booker ID.
     * Проверяет обработку null значения в поле booker.id.
     */
    @Test
    void toListDto_shouldHandleNullBookerId() {
        booker.setId(null);
        booking.setBooker(booker);

        BookingListDto result = bookingMapper.toListDto(booking);

        assertNotNull(result);
        assertNull(result.getBookerId());
        assertEquals(booker.getName(), result.getBookerName());
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
    }

    /**
     * Тест преобразования Booking в BookingListDto с null Booker Name.
     * Проверяет обработку null значения в поле booker.name.
     */
    @Test
    void toListDto_shouldHandleNullBookerName() {
        booker.setName(null);
        booking.setBooker(booker);

        BookingListDto result = bookingMapper.toListDto(booking);

        assertNotNull(result);
        assertNull(result.getBookerName());
        assertEquals(booker.getId(), result.getBookerId());
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
    }

    /**
     * Тест преобразования BookItemRequestDto в Booking.
     * Проверяет успешное преобразование всех полей.
     */
    @Test
    void toBooking_shouldConvertBookItemRequestDtoToBooking() {
        Booking result = bookingMapper.toBooking(bookingRequestDto, item, booker);

        assertNotNull(result);
        assertEquals(bookingRequestDto.getStart(), result.getStart());
        assertEquals(bookingRequestDto.getEnd(), result.getEnd());
        assertEquals(item, result.getItem());
        assertEquals(booker, result.getBooker());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        assertNull(result.getId());
    }

    /**
     * Тест преобразования BookItemRequestDto в Booking с null параметрами.
     * Проверяет обработку null значений.
     */
    @Test
    void toBooking_shouldReturnNullWhenAllParametersAreNull() {
        Booking result = bookingMapper.toBooking(null, null, null);

        assertNull(result);
    }

    /**
     * Тест преобразования BookItemRequestDto в Booking с null DTO, но с валидными item и booker.
     * Проверяет обработку частично null значений.
     */
    @Test
    void toBooking_shouldCreateBookingWithNullDtoButValidItemAndBooker() {
        Booking result = bookingMapper.toBooking(null, item, booker);

        assertNotNull(result);
        assertNull(result.getStart());
        assertNull(result.getEnd());
        assertEquals(item, result.getItem());
        assertEquals(booker, result.getBooker());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    /**
     * Тест преобразования BookItemRequestDto в Booking с валидным DTO, но null item и booker.
     * Проверяет обработку частично null значений.
     */
    @Test
    void toBooking_shouldCreateBookingWithValidDtoButNullItemAndBooker() {
        Booking result = bookingMapper.toBooking(bookingRequestDto, null, null);

        assertNotNull(result);
        assertEquals(bookingRequestDto.getStart(), result.getStart());
        assertEquals(bookingRequestDto.getEnd(), result.getEnd());
        assertNull(result.getItem());
        assertNull(result.getBooker());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    /**
     * Тест преобразования BookItemRequestDto в Booking с пустыми датами.
     * Проверяет обработку граничных условий.
     */
    @Test
    void toBooking_shouldHandleNullDates() {
        bookingRequestDto.setStart(null);
        bookingRequestDto.setEnd(null);

        Booking result = bookingMapper.toBooking(bookingRequestDto, item, booker);

        assertNotNull(result);
        assertNull(result.getStart());
        assertNull(result.getEnd());
        assertEquals(item, result.getItem());
        assertEquals(booker, result.getBooker());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    /**
     * Тест преобразования Booking с пустыми значениями.
     * Проверяет обработку граничных условий.
     */
    @Test
    void toDto_shouldHandleBookingWithEmptyValues() {
        Booking emptyBooking = new Booking();
        emptyBooking.setId(null);
        emptyBooking.setStart(null);
        emptyBooking.setEnd(null);
        emptyBooking.setItem(null);
        emptyBooking.setBooker(null);
        emptyBooking.setStatus(null);

        BookingDto result = bookingMapper.toDto(emptyBooking);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getStart());
        assertNull(result.getEnd());
        assertNull(result.getItem());
        assertNull(result.getBooker());
        assertNull(result.getStatus());
    }

    /**
     * Тест преобразования BookingListDto с пустыми значениями.
     * Проверяет обработку граничных условий.
     */
    @Test
    void toListDto_shouldHandleBookingWithEmptyValues() {
        Booking emptyBooking = new Booking();
        emptyBooking.setId(null);
        emptyBooking.setStart(null);
        emptyBooking.setEnd(null);
        emptyBooking.setItem(null);
        emptyBooking.setBooker(null);
        emptyBooking.setStatus(null);

        BookingListDto result = bookingMapper.toListDto(emptyBooking);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getStart());
        assertNull(result.getEnd());
        assertNull(result.getItemId());
        assertNull(result.getItemName());
        assertNull(result.getBookerId());
        assertNull(result.getBookerName());
        assertNull(result.getStatus());
    }

    /**
     * Тест преобразования BookItemRequestDto с пустыми значениями.
     * Проверяет обработку граничных условий.
     */
    @Test
    void toBooking_shouldHandleBookItemRequestDtoWithEmptyValues() {
        BookItemRequestDto emptyDto = new BookItemRequestDto();
        emptyDto.setStart(null);
        emptyDto.setEnd(null);

        Booking result = bookingMapper.toBooking(emptyDto, item, booker);

        assertNotNull(result);
        assertNull(result.getStart());
        assertNull(result.getEnd());
        assertEquals(item, result.getItem());
        assertEquals(booker, result.getBooker());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }
}

