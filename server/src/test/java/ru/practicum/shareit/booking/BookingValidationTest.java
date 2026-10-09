package ru.practicum.shareit.booking;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BookingValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void bookingCreateDtoShouldNotAllowNullItemId() {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));

        Set<ConstraintViolation<BookingCreateDto>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .contains("itemId");
    }

    @Test
    void bookingCreateDtoShouldNotAllowNullStart() {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(1L);
        dto.setEnd(LocalDateTime.now().plusHours(2));

        Set<ConstraintViolation<BookingCreateDto>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .contains("start");
    }

    @Test
    void bookingCreateDtoShouldNotAllowNullEnd() {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusHours(1));

        Set<ConstraintViolation<BookingCreateDto>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .contains("end");
    }

    @Test
    void bookingStateFromShouldParseCaseInsensitively() {
        assertThat(BookingState.from("all")).isPresent();
        assertThat(BookingState.from("CURRENT")).isPresent();
        assertThat(BookingState.from("future")).isPresent();
    }

    @Test
    void bookingStateFromShouldReturnEmptyForUnknownValue() {
        assertThat(BookingState.from("broken_state")).isEmpty();
    }

    @Test
    void bookingStateFromShouldReturnEmptyForNull() {
        assertThat(BookingState.from(null)).isEmpty();
    }
}