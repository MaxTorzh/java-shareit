package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true " +
            "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))")
    Page<Item> searchAvailableItems(@Param("text") String text, Pageable pageable);

    @Query("SELECT i FROM Item i " +
            "LEFT JOIN FETCH i.owner " +
            "LEFT JOIN FETCH i.request " +
            "WHERE i.id = :itemId")
    Optional<Item> findItemWithDependencies(@Param("itemId") Long itemId);

    @Query("SELECT i FROM Item i " +
            "LEFT JOIN FETCH i.owner " +
            "LEFT JOIN FETCH i.request " +
            "WHERE i.id IN :itemIds")
    List<Item> findItemsWithDependenciesByIds(@Param("itemIds") List<Long> itemIds);

    @Query("SELECT i FROM Item i " +
            "LEFT JOIN FETCH i.owner " +
            "LEFT JOIN FETCH i.request " +
            "WHERE i.request.id = :requestId")
    List<Item> findItemsWithDependenciesByRequestId(@Param("requestId") Long requestId);
}
