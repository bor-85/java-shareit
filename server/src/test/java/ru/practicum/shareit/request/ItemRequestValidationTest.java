package ru.practicum.shareit.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void itemRequestShouldNotAllowBlankDescription() {
        ItemRequest request = new ItemRequest();
        request.setDescription(" ");
        request.setCreated(LocalDateTime.now());

        Set<ConstraintViolation<ItemRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("description");
    }

    @Test
    void itemRequestCreateDtoShouldNotAllowBlankDescription() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription(" ");

        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("description");
    }
}