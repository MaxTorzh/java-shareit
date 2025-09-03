package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(User user);

    List<User> findAll();

    void deleteById(Long userId);

    Boolean existsById(Long userId);
}
