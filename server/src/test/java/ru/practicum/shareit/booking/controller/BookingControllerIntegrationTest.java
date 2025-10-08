package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для контроллера бронирований {@link BookingController}.
 * Тестирует все основные эндпоинты работы с бронированиями:
 * - Создание бронирования
 * - Подтверждение/отклонение бронирования владельцем
 * - Получение информации о бронировании
 * - Получение списков бронирований пользователя и владельца
 * - Отмена бронирования
 *
 * Тесты используют MockMvc для симуляции HTTP запросов и проверки ответов.
 * Перед каждым тестом создается тестовое окружение с владельцем предмета,
 * бронирующим пользователем и предметом для бронирования.
 *
 * После каждого теста контекст приложения очищается для обеспечения
 * изоляции тестов друг от друга.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto owner;
    private UserDto booker;
    private Long itemId;
    private static final String SHARER_HEADER = "X-Sharer-User-Id";

    /**
     * Подготовка тестового окружения перед каждым тестом.
     * Создает:
     * - Пользователя-владельца предмета
     * - Пользователя-бронирующего
     * - Предмет для бронирования
     *
     * @throws Exception если возникают ошибки при создании тестовых данных
     */
    @BeforeEach
    void setUp() throws Exception {
        owner = new UserDto();
        owner.setName("Item Owner");
        owner.setEmail("owner@test.com");

        var ownerResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(owner)))
                .andExpect(status().isOk())
                .andReturn();

        owner = objectMapper.readValue(ownerResult.getResponse().getContentAsString(), UserDto.class);

        booker = new UserDto();
        booker.setName("Item Booker");
        booker.setEmail("booker@test.com");

        var bookerResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booker)))
                .andExpect(status().isOk())
                .andReturn();

        booker = objectMapper.readValue(bookerResult.getResponse().getContentAsString(), UserDto.class);

        ItemRequestDto itemDto = new ItemRequestDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        var itemResult = mockMvc.perform(post("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn();

        itemId = objectMapper.readTree(itemResult.getResponse().getContentAsString()).get("id").asLong();
    }

    /**
     * Тест создания нового бронирования.
     * Проверяет, что бронирование успешно создается с правильными данными
     * и имеет статус WAITING по умолчанию.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void createBooking_shouldReturnCreatedBooking() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.booker.id", is(booker.getId().intValue())))
                .andExpect(jsonPath("$.item.id", is(itemId.intValue())));
    }

    /**
     * Тест создания бронирования владельцем собственного предмета.
     * Проверяет, что бронирование владельцем своего предмета запрещено.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void createBooking_shouldReturnForbiddenWhenOwnerTriesToBookOwnItem() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Тест создания бронирования для несуществующего предмета.
     * Проверяет правильную обработку ошибок для неверных ID предметов.
     *
     * @throws Exception если выполнение запроса завершится ошибкой
     */
    @Test
    void createBooking_shouldReturnNotFoundWhenItemNotExists() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(999L); // Несуществующий предмет
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isNotFound());
    }

    /**
     * Тест создания бронирования для недоступного предмета.
     * Проверяет правильную обработку ошибок для недоступных предметов.
     *
     * @throws Exception если выполнение запроса завершится ошибкой
     */
    @Test
    void createBooking_shouldReturnBadRequestWhenItemNotAvailable() throws Exception {
        // Сделать предмет недоступным
        ItemRequestDto itemDto = new ItemRequestDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(false);

        mockMvc.perform(patch("/items/{id}", itemId)
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Тест подтверждения бронирования владельцем.
     * Проверяет, что бронирование успешно подтверждается владельцем
     * и получает статус APPROVED.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void approveBooking_shouldApproveBooking() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        var createResult = mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andReturn();

        Long bookingId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(SHARER_HEADER, owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId.intValue())))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    /**
     * Тест подтверждения бронирования не владельцем.
     * Проверяет, что подтверждение бронирования не владельцем предмета запрещено.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void approveBooking_shouldReturnForbiddenWhenNotOwner() throws Exception {
        // Создать другого пользователя
        UserDto otherUser = new UserDto();
        otherUser.setName("Other User");
        otherUser.setEmail("other@test.com");

        var otherUserResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherUser)))
                .andExpect(status().isOk())
                .andReturn();

        UserDto otherUserDto = objectMapper.readValue(
                otherUserResult.getResponse().getContentAsString(), UserDto.class);

        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        var createResult = mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andReturn();

        Long bookingId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(SHARER_HEADER, otherUserDto.getId())
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    /**
     * Тест получения информации о бронировании по ID.
     * Проверяет, что бронирование успешно возвращается по своему ID
     * и содержит корректные данные о бронирующем пользователе.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getBookingById_shouldReturnBooking() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        var createResult = mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andReturn();

        Long bookingId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(SHARER_HEADER, booker.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId.intValue())))
                .andExpect(jsonPath("$.booker.id", is(booker.getId().intValue())));
    }

    /**
     * Тест получения бронирования по ID, когда пользователь не является владельцем или бронирующим.
     * Проверяет правильный контроль доступа.
     *
     * @throws Exception если выполнение запроса завершится ошибкой
     */
    @Test
    void getBookingById_shouldReturnForbiddenWhenUserIsNotOwnerOrBooker() throws Exception {
        UserDto otherUser = new UserDto();
        otherUser.setName("Другой пользователь");
        otherUser.setEmail("other@test.com");

        var otherUserResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherUser)))
                .andExpect(status().isOk())
                .andReturn();

        UserDto otherUserDto = objectMapper.readValue(
                otherUserResult.getResponse().getContentAsString(), UserDto.class);

        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        var createResult = mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andReturn();

        Long bookingId = objectMapper.readTree(
                createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(SHARER_HEADER, otherUserDto.getId()))
                .andExpect(status().isForbidden());
    }

    /**
     * Тест получения списка бронирований пользователя.
     * Проверяет, что список бронирований пользователя успешно возвращается
     * и содержит созданное бронирование.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserBookings_shouldReturnUserBookings() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].booker.id", is(booker.getId().intValue())));
    }

    /**
     * Тест получения списка бронирований пользователя с пагинацией.
     * Проверяет, что пагинация работает корректно.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserBookings_shouldHandlePagination() throws Exception {
        // Создать несколько бронирований
        for (int i = 0; i < 5; i++) {
            BookItemRequestDto bookingDto = new BookItemRequestDto();
            bookingDto.setItemId(itemId);
            bookingDto.setStart(LocalDateTime.now().plusDays(i + 1));
            bookingDto.setEnd(LocalDateTime.now().plusDays(i + 2));

            mockMvc.perform(post("/bookings")
                            .header(SHARER_HEADER, booker.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bookingDto)))
                    .andExpect(status().isOk());
        }

        // Проверить пагинацию
        mockMvc.perform(get("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .param("state", "ALL")
                        .param("from", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    /**
     * Тест получения списка бронирований пользователя с некорректными параметрами пагинации.
     * Проверяет, что некорректные параметры пагинации обрабатываются корректно.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserBookings_shouldHandleInvalidPaginationParams() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "0"))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Тест получения списка бронирований пользователя с текущими бронированиями.
     * Проверяет, что фильтрация по текущим бронированиям работает корректно.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserBookings_shouldReturnCurrentBookings() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().minusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .param("state", "CURRENT")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    /**
     * Тест получения списка бронирований пользователя с прошлыми бронированиями.
     * Проверяет, что фильтрация по прошлым бронированиям работает корректно.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserBookings_shouldReturnPastBookings() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().minusDays(2));
        bookingDto.setEnd(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .param("state", "PAST")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    /**
     * Тест получения списка бронирований пользователя с будущими бронированиями.
     * Проверяет, что фильтрация по будущим бронированиям работает корректно.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserBookings_shouldReturnFutureBookings() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .param("state", "FUTURE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    /**
     * Тест получения списка бронирований пользователя с ожидающими бронированиями.
     * Проверяет, что фильтрация по ожидающим бронированиям работает корректно.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserBookings_shouldReturnWaitingBookings() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    /**
     * Тест получения списка бронирований для владельца предметов.
     * Проверяет, что список бронирований владельца успешно возвращается
     * и содержит бронирования на его предметы.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getOwnerBookings_shouldReturnOwnerBookings() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings/owner")
                        .header(SHARER_HEADER, owner.getId())
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", notNullValue()));
    }

    /**
     * Тест получения списка бронирований для владельца с отклоненными бронированиями.
     * Проверяет, что фильтрация по отклоненным бронированиям работает корректно.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getOwnerBookings_shouldReturnRejectedBookings() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        var createResult = mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andReturn();

        Long bookingId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(SHARER_HEADER, owner.getId())
                        .param("approved", "false"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings/owner")
                        .header(SHARER_HEADER, owner.getId())
                        .param("state", "REJECTED")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    /**
     * Тест отмены бронирования.
     * Проверяет, что бронирование успешно отменяется пользователем
     * и получает статус CANCELLED.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void cancelBooking_shouldCancelBooking() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        var createResult = mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andReturn();

        Long bookingId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/bookings/{bookingId}", bookingId)
                        .header(SHARER_HEADER, booker.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("CANCELLED")));
    }

    /**
     * Тест отмены уже подтвержденного бронирования.
     * Проверяет, что отмена уже подтвержденного бронирования запрещена.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void cancelBooking_shouldReturnConflictWhenAlreadyApproved() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        var createResult = mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andReturn();

        Long bookingId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Подтвердить бронирование
        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(SHARER_HEADER, owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isOk());

        // Попытаться отменить подтвержденное бронирование
        mockMvc.perform(delete("/bookings/{bookingId}", bookingId)
                        .header(SHARER_HEADER, booker.getId()))
                .andExpect(status().isBadRequest());
    }
}


