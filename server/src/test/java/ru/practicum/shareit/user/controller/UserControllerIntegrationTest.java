package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для контроллера пользователей {@link UserController}.
 * Тестирует все основные эндпоинты работы с пользователями:
 * - Создание пользователя
 * - Получение информации о пользователе по ID
 * - Получение списка всех пользователей
 * - Обновление пользователя
 * - Удаление пользователя
 *
 * Тесты используют MockMvc для симуляции HTTP запросов и проверки ответов.
 * Класс использует реальную базу данных в памяти (H2) для хранения тестовых данных.
 *
 * Класс использует аннотацию {@link DirtiesContext} для очистки контекста
 * после каждого теста, обеспечивая независимость тестов друг от друга.
 *
 * Тесты проверяют как успешные сценарии, так и обработку ошибок,
 * включая конфликты по email и попытки доступа к несуществующим пользователям.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Тест создания нового пользователя.
     * Проверяет, что пользователь успешно создается с правильными данными
     * и возвращается в ответе с присвоенным ID.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Test User");
        userDto.setEmail("user@test.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Test User")))
                .andExpect(jsonPath("$.email", is("user@test.com")));
    }

    /**
     * Тест создания пользователя с уже существующим email.
     * Проверяет, что при попытке создания пользователя с email,
     * который уже используется другим пользователем, возвращается
     * HTTP статус 409 (Conflict).
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void createUser_shouldReturnConflictWhenEmailExists() throws Exception {
        UserDto userDto1 = new UserDto();
        userDto1.setName("Test User 1");
        userDto1.setEmail("user@test.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isOk());

        UserDto userDto2 = new UserDto();
        userDto2.setName("Test User 2");
        userDto2.setEmail("user@test.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto2)))
                .andExpect(status().isConflict());
    }

    /**
     * Тест получения информации о пользователе по ID.
     * Проверяет, что существующий пользователь успешно возвращается
     * с полной информацией.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserById_shouldReturnUser() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Test User");
        userDto.setEmail("user@test.com");

        var createResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn();

        Long userId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.intValue())))
                .andExpect(jsonPath("$.name", is("Test User")))
                .andExpect(jsonPath("$.email", is("user@test.com")));
    }

    /**
     * Тест получения несуществующего пользователя по ID.
     * Проверяет, что при попытке получить пользователя с несуществующим ID
     * возвращается HTTP статус 404 (Not Found).
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getUserById_shouldReturnNotFoundWhenUserNotExists() throws Exception {
        mockMvc.perform(get("/users/{userId}", 1000L))
                .andExpect(status().isNotFound());
    }

    /**
     * Тест получения списка всех пользователей.
     * Проверяет, что список пользователей успешно возвращается
     * и содержит созданных пользователей.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void getAllUsers_shouldReturnUsersList() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Test User");
        userDto.setEmail("user@test.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test User")));
    }

    /**
     * Тест обновления пользователя.
     * Проверяет, что пользователь успешно обновляется с новыми данными
     * и возвращается в ответе с обновленной информацией.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void updateUser_shouldUpdateAndReturnUser() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Original User");
        userDto.setEmail("original@test.com");

        var createResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn();

        Long userId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        UserDto updateUserDto = new UserDto();
        updateUserDto.setName("Updated User");
        updateUserDto.setEmail("updated@test.com");

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.intValue())))
                .andExpect(jsonPath("$.name", is("Updated User")))
                .andExpect(jsonPath("$.email", is("updated@test.com")));
    }

    /**
     * Тест обновления пользователя с конфликтующим email.
     * Проверяет, что при попытке обновить пользователя с email,
     * который уже используется другим пользователем, возвращается
     * HTTP статус 409 (Conflict).
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void updateUser_shouldReturnConflictWhenEmailExists() throws Exception {
        UserDto userDto1 = new UserDto();
        userDto1.setName("Test User 1");
        userDto1.setEmail("user1@test.com");

        var createResult1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isOk())
                .andReturn();

        Long userId1 = objectMapper.readTree(createResult1.getResponse().getContentAsString()).get("id").asLong();

        UserDto userDto2 = new UserDto();
        userDto2.setName("Test User 2");
        userDto2.setEmail("user2@test.com");

        var createResult2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto2)))
                .andExpect(status().isOk())
                .andReturn();

        Long userId2 = objectMapper.readTree(createResult2.getResponse().getContentAsString()).get("id").asLong();

        UserDto updateUserDto = new UserDto();
        updateUserDto.setEmail("user1@test.com");

        mockMvc.perform(patch("/users/{userId}", userId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isConflict());
    }

    /**
     * Тест удаления пользователя.
     * Проверяет, что пользователь успешно удаляется и становится недоступен
     * для последующего получения.
     *
     * @throws Exception если возникают ошибки при выполнении запроса
     */
    @Test
    void deleteUser_shouldRemoveUser() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Test User");
        userDto.setEmail("user@test.com");

        var createResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn();

        Long userId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isNotFound());
    }
}

