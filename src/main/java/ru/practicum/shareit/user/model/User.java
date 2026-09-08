package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static ru.practicum.shareit.exception.UserValidationMessages.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;

    @NotBlank(message = ERROR_INVALID_NAME)
    private String name;

    @NotBlank(message = ERROR_EMAIL_EMPTY)
    @Email(message = ERROR_INVALID_EMAIL)
    private String email;
}
