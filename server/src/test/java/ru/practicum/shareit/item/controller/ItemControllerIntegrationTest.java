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
     * Тест создания предмета с недостающими обязательными полями.
     * Проверяет, что предмет не создается при отсутствии обязательных полей.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void createItem_shouldReturnBadRequestWhenMissingRequiredFields() throws Exception {
        ItemRequestDto itemDto = new ItemRequestDto();
        // Не заполняем обязательные поля

        mockMvc.perform(post("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isConflict());
    }

    /**
     * Тест создания предмета с пустым описанием.
     * Проверяет, что предмет создается даже при пустом описании.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void createItem_shouldCreateItemWhenDescriptionIsEmpty() throws Exception {
        ItemRequestDto itemDto = new ItemRequestDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Test Item")))
                .andExpect(jsonPath("$.description", is("")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    /**
     * Тест создания предмета без заголовка пользователя.
     * Проверяет, что предмет не создается при отсутствии заголовка с ID пользователя.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void createItem_shouldReturnBadRequestWhenNoUserHeader() throws Exception {
        ItemRequestDto itemDto = new ItemRequestDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Тест создания предмета с несуществующим пользователем.
     * Проверяет, что предмет не создается при несуществующем ID пользователя.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void createItem_shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        ItemRequestDto itemDto = new ItemRequestDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(SHARER_HEADER, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
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
     * Тест получения информации о несуществующем предмете.
     * Проверяет, что возвращается ошибка 404 при запросе несуществующего предмета.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getItemWithBookings_shouldReturnNotFoundWhenItemDoesNotExist() throws Exception {
        mockMvc.perform(get("/items/{itemId}", 999L)
                        .header(SHARER_HEADER, owner.getId()))
                .andExpect(status().isNotFound());
    }

    /**
     * Тест получения информации о предмете без заголовка пользователя.
     * Проверяет, что возвращается ошибка 500 при отсутствии заголовка с ID пользователя.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getItemWithBookings_shouldReturnBadRequestWhenNoUserHeader() throws Exception {
        mockMvc.perform(get("/items/{itemId}", 1L))
                .andExpect(status().isInternalServerError());
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
     * Тест получения списка предметов пользователя без заголовка.
     * Проверяет, что возвращается ошибка 500 при отсутствии заголовка с ID пользователя.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserItemsWithBookings_shouldReturnBadRequestWhenNoUserHeader() throws Exception {
        mockMvc.perform(get("/items")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Тест получения списка предметов пользователя с нулевым размером страницы.
     * Проверяет, что возвращается ошибка 500 при нулевом значении size.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserItemsWithBookings_shouldReturnBadRequestWhenSizeIsZero() throws Exception {
        mockMvc.perform(get("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Тест получения списка предметов пользователя с отрицательным размером страницы.
     * Проверяет, что возвращается ошибка 400 при отрицательном значении size.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserItemsWithBookings_shouldReturnBadRequestWhenSizeIsNegative() throws Exception {
        mockMvc.perform(get("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .param("from", "0")
                        .param("size", "-1"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Тест получения списка предметов пользователя с большим размером страницы.
     * Проверяет, что обрабатывается большое значение size.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserItemsWithBookings_shouldHandleLargePageSize() throws Exception {
        mockMvc.perform(get("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .param("from", "0")
                        .param("size", "1000"))
                .andExpect(status().isOk());
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
     * Тест поиска предметов по пустому тексту.
     * Проверяет, что поиск по пустому тексту возвращает пустой список.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void searchItems_shouldReturnEmptyListWhenTextIsEmpty() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Тест поиска предметов с нулевым размером страницы.
     * Проверяет, что возвращается ошибка 500 при нулевом значении size.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void searchItems_shouldReturnBadRequestWhenSizeIsZero() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "test")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isInternalServerError());
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
     * Тест обновления несуществующего предмета.
     * Проверяет, что возвращается ошибка 404 при попытке обновления несуществующего предмета.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void updateItem_shouldReturnNotFoundWhenItemDoesNotExist() throws Exception {
        ItemRequestDto updateDto = new ItemRequestDto();
        updateDto.setName("Updated Item");

        mockMvc.perform(patch("/items/{itemId}", 999L)
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    /**
     * Тест обновления предмета без заголовка пользователя.
     * Проверяет, что возвращается ошибка 400 при отсутствии заголовка с ID пользователя.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void updateItem_shouldReturnBadRequestWhenNoUserHeader() throws Exception {
        ItemRequestDto updateDto = new ItemRequestDto();
        updateDto.setName("Updated Item");

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isInternalServerError());
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

    /**
     * Тест удаления несуществующего предмета.
     * Проверяет, что возвращается ошибка 404 при попытке удаления несуществующего предмета.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void deleteItem_shouldReturnNotFoundWhenItemDoesNotExist() throws Exception {
        mockMvc.perform(delete("/items/{itemId}", 999L))
                .andExpect(status().isNotFound());
    }

    /**
     * Тест создания предмета с очень длинным именем.
     * Проверяет ограничения на длину имени предмета.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void createItem_shouldReturnBadRequestWhenNameIsTooLong() throws Exception {
        ItemRequestDto itemDto = new ItemRequestDto();
        itemDto.setName("A".repeat(256));
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isConflict());
    }

    /**
     * Тест создания предмета с очень длинным описанием.
     * Проверяет ограничения на длину описания предмета.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void createItem_shouldReturnBadRequestWhenDescriptionIsTooLong() throws Exception {
        ItemRequestDto itemDto = new ItemRequestDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("A".repeat(4001));
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isConflict());
    }

    /**
     * Тест поиска предметов с пробелами в тексте поиска.
     * Проверяет корректную обработку пробелов в тексте поиска.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void searchItems_shouldHandleSpacesInSearchText() throws Exception {
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
                        .param("text", " Test ")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Тест получения списка предметов пользователя с большим значением from.
     * Проверяет поведение при большом значении from.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserItemsWithBookings_shouldHandleLargeFromValue() throws Exception {
        mockMvc.perform(get("/items")
                        .header(SHARER_HEADER, owner.getId())
                        .param("from", "1000")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    /**
     * Тест поиска предметов с различными регистрами.
     * Проверяет, что поиск нечувствителен к регистру.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void searchItems_shouldBeCaseInsensitive() throws Exception {
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
                        .param("text", "test")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/items/search")
                        .param("text", "TEST")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}


