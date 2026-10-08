package ru.practicum.shareit.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.validation.UserValidationMessages.*;

@SpringBootTest
class UserValidationTest {

    @Autowired
    private Validator validator;

    @Test
    void userShouldNotAllowBlankName() {
        User user = new User(null, "", "ivan@mail.ru");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(ERROR_INVALID_NAME);
    }

    @Test
    void userShouldNotAllowBlankEmail() {
        User user = new User(null, "Ivan", "");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(ERROR_EMAIL_EMPTY);
    }

    @Test
    void userShouldNotAllowInvalidEmail() {
        User user = new User(null, "Ivan", "wrong-email");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(ERROR_INVALID_EMAIL);
    }

    @Test
    void userDtoShouldValidateNameAndEmail() {
        UserDto dto = new UserDto();
        dto.setName("");
        dto.setEmail("wrong-email");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(ERROR_INVALID_NAME, ERROR_INVALID_EMAIL);
    }

    @Test
    void userUpdateDtoShouldValidateEmail() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("");

        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(ERROR_EMAIL_EMPTY);
    }
}