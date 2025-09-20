package ru.practicum.shareit.user.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserValidator {
    private final UserRepository userRepository;

    public void updateUserFields(User existingUser, User newUser) {
        if (newUser.getName() != null) {
            existingUser.setName(newUser.getName());
        }
        if (newUser.getEmail() != null &&
                !newUser.getEmail().equals(existingUser.getEmail())) {
            checkUniqueEmail(newUser.getEmail());
            existingUser.setEmail(newUser.getEmail());
        }
    }

    public void validateExistingUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID: " + userId + " не найден");
        }
    }

    public void checkUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Такой email уже существует: " + email);
        }
    }

    public User saveUser(User user, String operation) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Ошибка при " + operation + ": " + user.getEmail());
        }
    }
}
