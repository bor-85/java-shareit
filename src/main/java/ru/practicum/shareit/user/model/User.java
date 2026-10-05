package ru.practicum.shareit.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static ru.practicum.shareit.exception.UserValidationMessages.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uq_users_email", columnNames = "email"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = ERROR_INVALID_NAME)
    @Column(nullable = false)
    private String name;

    @NotBlank(message = ERROR_EMAIL_EMPTY)
    @Email(message = ERROR_INVALID_EMAIL)
    @Column(nullable = false, unique = true, length = 512)
    private String email;
}