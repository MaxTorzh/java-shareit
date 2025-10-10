package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestsDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapperImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.mapper.UserMapperImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Slice тесты для сервиса запросов предметов {@link ItemRequestServiceImpl}.
 * Тестируют функциональность сервиса с использованием реальной базы данных
 * и моков для внешних сервисов (UserService).
 *
 * Используют @DataJpaTest для тестирования слоя работы с БД и @Import для
 * загрузки тестируемого сервиса и его зависимостей.
 *
 * Класс использует аннотацию {@link DirtiesContext} для очистки контекста
 * после каждого теста, обеспечивая независимость тестов друг от друга.
 *
 * Тесты импортируют необходимые мапперы для корректной работы сервиса
 * с преобразованием сущностей в DTO и обратно.
 */
@DataJpaTest
@Import({ItemRequestServiceImpl.class, ItemRequestMapperImpl.class, ItemMapperImpl.class, UserMapperImpl.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestServiceImplSliceTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @MockBean
    private UserService userService;

    private User requester;
    private ItemRequest itemRequest;
    private Item item;

    /**
     * Подготовка тестового окружения перед каждым тестом.
     * Создает тестовые сущности в базе данных:
     * - Пользователя-запрашивающего
     * - Запрос на предмет
     * - Предмет, связанный с запросом
     * - Настраивает моки внешних сервисов
     *
     * Моки настроены для возврата созданного пользователя при запросе по ID
     * и выбрасывания исключения при запросе несуществующего пользователя.
     */
    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setName("Requester");
        requester.setEmail("requester@test.com");
        requester = entityManager.persistAndFlush(requester);

        when(userService.getUserById(requester.getId())).thenReturn(requester);
        when(userService.getUserById(999L)).thenThrow(new NotFoundException("User not found"));

        itemRequest = new ItemRequest();
        itemRequest.setDescription("Нужна дрель");
        itemRequest.setRequester(requester);
        itemRequest.setCreatedTime(LocalDateTime.now());
        itemRequest = entityManager.persistAndFlush(itemRequest);

        item = new Item();
        item.setName("Дрель");
        item.setDescription("Электрическая дрель");
        item.setAvailable(true);
        item.setOwner(requester);
        item.setRequest(itemRequest);
        item = entityManager.persistAndFlush(item);
    }

    /**
     * Тест создания нового запроса на предмет.
     * Проверяет, что запрос успешно создается в базе данных
     * с правильными данными и временем создания.
     */
    @Test
    void createRequest_shouldCreateAndReturnRequest() {
        ItemRequest newRequest = new ItemRequest();
        newRequest.setDescription("Нужна отвертка");
        newRequest.setRequester(requester);
        newRequest.setCreatedTime(LocalDateTime.now());

        ItemRequest createdRequest = itemRequestService.createRequest(newRequest);

        assertNotNull(createdRequest.getId());
        assertEquals("Нужна отвертка", createdRequest.getDescription());
        assertEquals(requester.getId(), createdRequest.getRequester().getId());
        assertNotNull(createdRequest.getCreatedTime());
    }

    /**
     * Тест получения запроса по ID.
     * Проверяет, что существующий запрос успешно возвращается
     * с правильными данными.
     */
    @Test
    void getRequestById_shouldReturnRequestWhenExists() {
        ItemRequest foundRequest = itemRequestService.getRequestById(itemRequest.getId());

        assertNotNull(foundRequest);
        assertEquals(itemRequest.getId(), foundRequest.getId());
        assertEquals(itemRequest.getDescription(), foundRequest.getDescription());
    }

    /**
     * Тест получения несуществующего запроса по ID.
     * Проверяет, что при попытке получить несуществующий запрос
     * выбрасывается исключение {@link NotFoundException}.
     */
    @Test
    void getRequestById_shouldThrowNotFoundExceptionWhenNotExists() {
        Long nonExistingId = 999L;

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(nonExistingId));
    }

    /**
     * Тест получения DTO запроса с предметами.
     * Проверяет, что запрос успешно возвращается в виде DTO
     * с полной информацией и связанными предметами.
     */
    @Test
    void getItemRequestDtoById_shouldReturnRequestWithItems() {
        ItemRequestsDto dto = itemRequestService.getItemRequestDtoById(itemRequest.getId());

        assertNotNull(dto);
        assertEquals(itemRequest.getId(), dto.getId());
        assertEquals(itemRequest.getDescription(), dto.getDescription());
        assertEquals(requester.getId(), dto.getRequesterId());
        assertNotNull(dto.getCreated());

        assertNotNull(dto.getItems());
        assertEquals(1, dto.getItems().size());
        assertEquals(item.getId(), dto.getItems().get(0).getId());
        assertEquals(item.getName(), dto.getItems().get(0).getName());
    }

    /**
     * Тест получения списка запросов пользователя в виде DTO.
     * Проверяет, что список запросов пользователя успешно возвращается
     * и содержит созданные запросы.
     */
    @Test
    void getUserRequestsDtoList_shouldReturnUserRequests() {
        Pageable pageable = PageRequest.of(0, 10);

        List<ItemRequestsDto> userRequests = itemRequestService.getUserRequestsDtoList(requester.getId(), pageable);

        assertNotNull(userRequests);
        assertEquals(1, userRequests.size());
        assertEquals(itemRequest.getId(), userRequests.get(0).getId());
        assertEquals(itemRequest.getDescription(), userRequests.get(0).getDescription());
    }

    /**
     * Тест получения всех запросов в виде DTO.
     * Проверяет, что список всех запросов успешно возвращается.
     */
    @Test
    void getAllRequestsDtoList_shouldReturnAllRequests() {
        Pageable pageable = PageRequest.of(0, 10);

        List<ItemRequestsDto> allRequests = itemRequestService.getAllRequestsDtoList(pageable);

        assertNotNull(allRequests);
        assertEquals(1, allRequests.size());
        assertEquals(itemRequest.getId(), allRequests.get(0).getId());
    }

    /**
     * Тест получения запросов других пользователей в виде DTO.
     * Проверяет, что список запросов других пользователей успешно возвращается
     * и не содержит запросов текущего пользователя.
     */
    @Test
    void getOtherUserRequestsDtoList_shouldReturnOtherUsersRequests() {
        User otherUser = new User();
        otherUser.setName("Other User");
        otherUser.setEmail("other@test.com");
        otherUser = entityManager.persistAndFlush(otherUser);

        when(userService.getUserById(otherUser.getId())).thenReturn(otherUser);

        Pageable pageable = PageRequest.of(0, 10);

        List<ItemRequestsDto> otherRequests = itemRequestService.getOtherUserRequestsDtoList(otherUser.getId(), pageable);

        assertNotNull(otherRequests);
        assertEquals(1, otherRequests.size());
        assertEquals(itemRequest.getId(), otherRequests.get(0).getId());
        assertEquals(itemRequest.getDescription(), otherRequests.get(0).getDescription());
    }
}



