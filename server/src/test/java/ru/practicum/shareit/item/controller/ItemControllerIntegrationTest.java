package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для контроллера предметов {@link ItemController}.
 * Тестирует все основные эндпоинты работы с предметами:
 * - Создание предмета
 * - Получение информации о предмете
 * - Получение списка предметов пользователя
 * - Поиск предметов по тексту
 * - Обновление предмета
 * - Удаление предмета
 *
 * Тесты используют MockMvc для симуляции HTTP запросов и проверки ответов.
 * Используется реальная база данных в памяти (H2) для хранения тестовых данных.
 *
 * Класс использует аннотацию {@link DirtiesContext} для очистки контекста
 * после каждого теста, обеспечивая независимость тестов друг от друга.
 *
 * Перед каждым тестом создается тестовый пользователь-владелец,
 * который используется для выполнения операций с предметами.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemControllerIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    private User owner;
    private static final String SHARER_HEADER = "X-Sharer-User-Id";

    /**
     * Подготовка тестового окружения перед каждым тестом.
     * Создает тестового пользователя-владельца, который будет использоваться
     * для выполнения операций с предметами в тестах.
     */
    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Test Owner");
        owner.setEmail("owner@test.com");
        owner = userRepository.save(owner);
    }

    /**
     * Тест создания нового предмета.
     * Проверяет, что предмет успешно создается с правильными данными
     * и возвращается в ответе с присвоенным ID.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        ItemRequestDto itemDto = new ItemRequestDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Test Item")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.ownerId", is(owner.getId().intValue())));
    }

    /**
     * Тест получения информации о предмете с бронированиями.
     * Проверяет, что предмет успешно возвращается по своему ID
     * с полной информацией, включая возможные бронирования.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getItemWithBookings_shouldReturnItem() throws Exception {
        ItemRequestDto itemDto = new ItemRequestDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        var createResult = mockMvc.perform(post("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn();

        Long itemId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(SHARER_HEADER, owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemId.intValue())))
                .andExpect(jsonPath("$.name", is("Test Item")));
    }

    /**
     * Тест получения списка предметов пользователя с бронированиями.
     * Проверяет, что список всех предметов пользователя успешно возвращается
     * и содержит созданные предметы.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserItemsWithBookings_shouldReturnUserItems() throws Exception {
        ItemRequestDto itemDto = new ItemRequestDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Item")));
    }

    /**
     * Тест поиска предметов по тексту.
     * Проверяет, что поиск предметов по текстовому запросу
     * успешно возвращает подходящие предметы.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void searchItems_shouldReturnMatchingItems() throws Exception {
        ItemRequestDto itemDto = new ItemRequestDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/items/search")
                        .param("text", "Test")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Item")));
    }

    /**
     * Тест обновления предмета.
     * Проверяет, что предмет успешно обновляется с новыми данными
     * и возвращается в ответе с обновленной информацией.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void updateItem_shouldUpdateAndReturnItem() throws Exception {
        // Создаем предмет
        ItemRequestDto itemDto = new ItemRequestDto();
        itemDto.setName("Original Item");
        itemDto.setDescription("Original Description");
        itemDto.setAvailable(true);

        var createResult = mockMvc.perform(post("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn();

        Long itemId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        ItemRequestDto updateDto = new ItemRequestDto();
        updateDto.setName("Updated Item");
        updateDto.setDescription("Updated Description");
        updateDto.setAvailable(false);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemId.intValue())))
                .andExpect(jsonPath("$.name", is("Updated Item")))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.available", is(false)));
    }

    /**
     * Тест удаления предмета.
     * Проверяет, что предмет успешно удаляется и становится недоступен
     * для последующего получения.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void deleteItem_shouldRemoveItem() throws Exception {
        // Создаем предмет
        ItemRequestDto itemDto = new ItemRequestDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        var createResult = mockMvc.perform(post("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn();

        Long itemId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/items/{itemId}", itemId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(SHARER_HEADER, owner.getId()))
                .andExpect(status().isNotFound());
    }
}

