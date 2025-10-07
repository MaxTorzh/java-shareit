package ru.practicum.shareit.user.validator;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

@Component
public class UserValidator {

    public void validateUserCreation(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
        if (!isValidEmail(userDto.getEmail())) {
            throw new ValidationException("Email должен содержать символ @");
        }
    }

    public void validateUserUpdate(Long userId, UserDto userDto) {
        if (userId == null || userId < 0) {
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            if (!isValidEmail(userDto.getEmail())) {
                throw new ValidationException("Email должен содержать символ @");
            }
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }
}
