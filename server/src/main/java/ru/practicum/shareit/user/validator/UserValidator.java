package ru.practicum.shareit.user.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserValidator {
    private final UserRepository repository;

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
        if (!repository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID: " + userId + " не найден");
        }
    }

    public void validateUserCreation(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
        if (!isValidEmail(user.getEmail())) {
            throw new ValidationException("Email должен содержать символ @");
        }
        if (repository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Такой email уже существует: " + user.getEmail());
        }
    }

    public void checkUniqueEmail(String email) {
        if (repository.existsByEmail(email)) {
            throw new ConflictException("Такой email уже существует: " + email);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }
}

