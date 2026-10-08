package ru.practicum.shareit.item;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.validation.ItemValidationMessages.*;

class ItemValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void itemShouldNotAllowBlankName() {
        Item item = new Item(null, "", "Description", true, null, null);

        Set<ConstraintViolation<Item>> violations = validator.validate(item);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(ERROR_NAME_EMPTY);
    }

    @Test
    void itemShouldNotAllowBlankDescription() {
        Item item = new Item(null, "Name", "", true, null, null);

        Set<ConstraintViolation<Item>> violations = validator.validate(item);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(ERROR_DESCRIPTION_EMPTY);
    }

    @Test
    void itemDtoShouldNotAllowBlankNameDescriptionAndNullAvailable() {
        ItemDto dto = new ItemDto();
        dto.setName("");
        dto.setDescription("");
        dto.setAvailable(null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(ERROR_NAME_EMPTY, ERROR_DESCRIPTION_EMPTY, ERROR_AVAILABLE_EMPTY);
    }

    @Test
    void itemUpdateDtoShouldNotAllowBlankName() {
        ItemUpdateDto dto = new ItemUpdateDto();
        dto.setName("   ");

        Set<ConstraintViolation<ItemUpdateDto>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(ERROR_NAME_EMPTY);
    }

    @Test
    void itemUpdateDtoShouldNotAllowBlankDescription() {
        ItemUpdateDto dto = new ItemUpdateDto();
        dto.setDescription("   ");

        Set<ConstraintViolation<ItemUpdateDto>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(ERROR_DESCRIPTION_EMPTY);
    }

    @Test
    void commentCreateDtoShouldNotAllowBlankText() {
        CommentCreateDto dto = new CommentCreateDto();
        dto.setText(" ");

        Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }
}