package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static ru.practicum.shareit.exception.UserValidationMessages.*;

@Data
public class UserDto {
    private Long id;

    @NotBlank(message = ERROR_INVALID_NAME)
    private String name;

    @NotBlank(message = ERROR_EMAIL_EMPTY)
    @Email(message = ERROR_INVALID_EMAIL)
    private String email;
}
