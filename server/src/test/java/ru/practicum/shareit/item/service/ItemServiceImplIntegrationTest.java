package ru.practicum.shareit.item.service;

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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.validator.ItemValidator;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Интеграционные тесты для сервиса предметов {@link ItemServiceImpl}.
 * Тестирует все основные функции сервиса предметов:
 * - Создание предмета
 * - Получение предмета по ID
 * - Обновление предмета
 * - Получение списка предметов пользователя
 * - Поиск предметов по тексту
 * - Удаление предмета
 *
 * Тесты используют реальную базу данных в памяти (H2) через TestEntityManager
 * и моки внешних сервисов (UserService) для изоляции тестируемого функционала.
 *
 * Класс использует аннотацию {@link DirtiesContext} для очистки контекста
 * после каждого теста, обеспечивая независимость тестов друг от друга.
 */
@DataJpaTest
@Import({ItemServiceImpl.class, ItemValidator.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRepository itemRepository;

    @MockBean
    private UserService userService;

    private User owner;
    private Item item;

    /**
     * Подготовка тестового окружения перед каждым тестом.
     * Создает тестовые сущности в базе данных:
     * - Пользователя-владельца предмета
     * - Предмет для тестирования
     * - Настраивает моки внешних сервисов
     *
     * Моки настроены для возврата созданного пользователя при запросе по ID
     * и выбрасывания исключения при запросе несуществующего пользователя.
     */
    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Test Owner");
        owner.setEmail("owner@test.com");
        owner = entityManager.persistAndFlush(owner);

        when(userService.getUserById(1L)).thenReturn(owner);
        when(userService.getUserById(999L)).thenThrow(new NotFoundException("User not found"));

        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
    }

    /**
     * Тест создания нового предмета.
     * Проверяет, что предмет успешно создается в базе данных
     * с правильными данными и присвоенным ID.
     */
    @Test
    void createItem_shouldCreateAndReturnItem() {
        Item createdItem = itemService.createItem(item);

        assertNotNull(createdItem.getId());
        assertEquals("Test Item", createdItem.getName());
        assertEquals("Test Description", createdItem.getDescription());
        assertTrue(createdItem.getAvailable());
        assertEquals(owner.getId(), createdItem.getOwner().getId());
    }

    /**
     * Тест получения предмета по ID.
     * Проверяет, что существующий предмет успешно возвращается
     * с правильными данными.
     */
    @Test
    void getItemById_shouldReturnItemWhenExists() {
        Item savedItem = itemRepository.save(item);

        Item foundItem = itemService.getItemById(savedItem.getId());

        assertNotNull(foundItem);
        assertEquals(savedItem.getId(), foundItem.getId());
        assertEquals(savedItem.getName(), foundItem.getName());
    }

    /**
     * Тест получения несуществующего предмета по ID.
     * Проверяет, что при попытке получить несуществующий предмет
     * выбрасывается исключение {@link NotFoundException}.
     */
    @Test
    void getItemById_shouldThrowNotFoundExceptionWhenNotExists() {
        Long nonExistingItemId = 999L;

        assertThrows(NotFoundException.class, () -> itemService.getItemById(nonExistingItemId));
    }

    /**
     * Тест обновления предмета.
     * Проверяет, что предмет успешно обновляется с новыми данными
     * и изменения сохраняются в базе данных.
     */
    @Test
    void updateItem_shouldUpdateItemFields() {
        Item savedItem = itemRepository.save(item);

        Item updateItem = new Item();
        updateItem.setName("Updated Name");
        updateItem.setDescription("Updated Description");
        updateItem.setAvailable(false);
        updateItem.setOwner(owner);

        Item updatedItem = itemService.updateItem(savedItem.getId(), updateItem);

        assertEquals("Updated Name", updatedItem.getName());
        assertEquals("Updated Description", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
    }

    /**
     * Тест получения списка предметов пользователя.
     * Проверяет, что список предметов пользователя успешно возвращается
     * и содержит созданные предметы.
     */
    @Test
    void getUserItems_shouldReturnUserItems() {
        Item savedItem = itemRepository.save(item);
        Pageable pageable = PageRequest.of(0, 10);

        var userItems = itemService.getUserItems(owner.getId(), pageable);

        assertEquals(1, userItems.getTotalElements());
        assertEquals(savedItem.getId(), userItems.getContent().get(0).getId());
    }

    /**
     * Тест поиска предметов по тексту с совпадением.
     * Проверяет, что поиск успешно возвращает предметы,
     * соответствующие текстовому запросу.
     */
    @Test
    void searchItems_shouldReturnMatchingItems() {
        itemRepository.save(item);

        Pageable pageable = PageRequest.of(0, 10);
        var searchResult = itemService.searchItems("Test", pageable);

        assertEquals(1, searchResult.getTotalElements());
        assertEquals("Test Item", searchResult.getContent().get(0).getName());
    }

    /**
     * Тест поиска предметов по тексту без совпадений.
     * Проверяет, что поиск возвращает пустой результат,
     * когда нет предметов, соответствующих запросу.
     */
    @Test
    void searchItems_shouldReturnEmptyWhenNoMatch() {
        itemRepository.save(item);

        Pageable pageable = PageRequest.of(0, 10);
        var searchResult = itemService.searchItems("NonExistent", pageable);

        assertEquals(0, searchResult.getTotalElements());
    }

    /**
     * Тест удаления предмета.
     * Проверяет, что предмет успешно удаляется из базы данных
     * и становится недоступен для последующего получения.
     */
    @Test
    void deleteItem_shouldRemoveItem() {
        Item savedItem = itemRepository.save(item);

        itemService.deleteItem(savedItem.getId());

        assertThrows(NotFoundException.class, () -> itemService.getItemById(savedItem.getId()));
    }
}
