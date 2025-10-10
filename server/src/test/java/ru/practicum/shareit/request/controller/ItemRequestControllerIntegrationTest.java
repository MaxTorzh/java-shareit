package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для контроллера запросов предметов {@link ItemRequestController}.
 * Тестирует все основные эндпоинты работы с запросами на добавление предметов:
 * - Создание запроса на предмет
 * - Получение информации о запросе по ID
 * - Получение списка запросов текущего пользователя
 * - Получение списка запросов других пользователей
 * - Получение всех запросов
 *
 * Тесты используют MockMvc для симуляции HTTP запросов и проверки ответов.
 * Перед каждым тестом создается тестовое окружение с двумя пользователями
 * для проверки функциональности запросов.
 *
 * Класс использует аннотацию {@link DirtiesContext} для очистки контекста
 * после каждого теста, обеспечивая независимость тестов друг от друга.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto user1;
    private UserDto user2;
    private static final String SHARER_HEADER = "X-Sharer-User-Id";

    /**
     * Подготовка тестового окружения перед каждым тестом.
     * Создает двух тестовых пользователей, которые будут использоваться
     * для тестирования функциональности запросов на добавление предметов.
     *
     * @throws Exception если возникают ошибки при создании тестовых данных
     */
    @BeforeEach
    void setUp() throws Exception {
        user1 = new UserDto();
        user1.setName("User 1");
        user1.setEmail("user1@test.com");

        var user1Result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk())
                .andReturn();

        user1 = objectMapper.readValue(user1Result.getResponse().getContentAsString(), UserDto.class);

        user2 = new UserDto();
        user2.setName("User 2");
        user2.setEmail("user2@test.com");

        var user2Result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isOk())
                .andReturn();

        user2 = objectMapper.readValue(user2Result.getResponse().getContentAsString(), UserDto.class);
    }

    /**
     * Тест создания нового запроса на предмет.
     * Проверяет, что запрос успешно создается с правильными данными
     * и возвращается в ответе с присвоенным ID и временем создания.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void createItemRequest_shouldReturnCreatedRequest() throws Exception {
        ItemRequestRequestDto requestDto = new ItemRequestRequestDto();
        requestDto.setDescription("Нужна дрель");

        mockMvc.perform(post("/requests")
                        .header(SHARER_HEADER, user1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.description", is("Нужна дрель")))
                .andExpect(jsonPath("$.requesterId", is(user1.getId().intValue())))
                .andExpect(jsonPath("$.created", notNullValue()));
    }

    /**
     * Тест получения информации о запросе по ID.
     * Проверяет, что существующий запрос успешно возвращается
     * с полной информацией о запросе и его создателе.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        ItemRequestRequestDto requestDto = new ItemRequestRequestDto();
        requestDto.setDescription("Нужна дрель");

        var createResult = mockMvc.perform(post("/requests")
                        .header(SHARER_HEADER, user1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        Long requestId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(SHARER_HEADER, user1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestId.intValue())))
                .andExpect(jsonPath("$.description", is("Нужна дрель")))
                .andExpect(jsonPath("$.requesterId", is(user1.getId().intValue())));
    }

    /**
     * Тест получения списка запросов текущего пользователя.
     * Проверяет, что список запросов пользователя успешно возвращается
     * и содержит созданные пользователем запросы.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserRequests_shouldReturnUserRequests() throws Exception {
        ItemRequestRequestDto requestDto = new ItemRequestRequestDto();
        requestDto.setDescription("Нужна дрель");

        mockMvc.perform(post("/requests")
                        .header(SHARER_HEADER, user1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/requests")
                        .header(SHARER_HEADER, user1.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is("Нужна дрель")));
    }

    /**
     * Тест получения списка запросов других пользователей.
     * Проверяет, что список запросов других пользователей успешно возвращается
     * и не содержит запросов текущего пользователя.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getOtherUserRequests_shouldReturnOtherUsersRequests() throws Exception {
        ItemRequestRequestDto requestDto1 = new ItemRequestRequestDto();
        requestDto1.setDescription("Нужна дрель");

        mockMvc.perform(post("/requests")
                        .header(SHARER_HEADER, user1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto1)))
                .andExpect(status().isOk());

        ItemRequestRequestDto requestDto2 = new ItemRequestRequestDto();
        requestDto2.setDescription("Нужна отвертка");

        mockMvc.perform(post("/requests")
                        .header(SHARER_HEADER, user2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/requests/other")
                        .header(SHARER_HEADER, user1.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is("Нужна отвертка")))
                .andExpect(jsonPath("$[0].requesterId", is(user2.getId().intValue())));
    }

    /**
     * Тест получения всех запросов.
     * Проверяет, что список всех запросов успешно возвращается
     * и содержит запросы всех пользователей системы.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getAllRequests_shouldReturnAllRequests() throws Exception {
        ItemRequestRequestDto requestDto1 = new ItemRequestRequestDto();
        requestDto1.setDescription("Нужна дрель");

        mockMvc.perform(post("/requests")
                        .header(SHARER_HEADER, user1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto1)))
                .andExpect(status().isOk());

        ItemRequestRequestDto requestDto2 = new ItemRequestRequestDto();
        requestDto2.setDescription("Нужна отвертка");

        mockMvc.perform(post("/requests")
                        .header(SHARER_HEADER, user2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/requests/all")
                        .header(SHARER_HEADER, user1.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}

