package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static ru.practicum.shareit.exception.UserValidationMessages.*;

@Data
public class UserUpdateDto {

    @Size(min = 1, message = ERROR_INVALID_NAME)
    private String name;

    @Size(min = 1, message = ERROR_EMAIL_EMPTY)
    @Email(message = ERROR_INVALID_EMAIL)
    private String email;
}