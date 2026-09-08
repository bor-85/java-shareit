package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import static ru.practicum.shareit.exception.ItemValidationMessages.ERROR_DESCRIPTION_EMPTY;
import static ru.practicum.shareit.exception.ItemValidationMessages.ERROR_NAME_EMPTY;

@Data
public class ItemUpdateDto {

    @Pattern(regexp = ".*\\S.*", message = ERROR_NAME_EMPTY)
    private String name;

    @Pattern(regexp = ".*\\S.*", message = ERROR_DESCRIPTION_EMPTY)
    private String description;

    private Boolean available;
}