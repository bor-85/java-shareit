package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import static ru.practicum.shareit.exception.ItemValidationMessages.ERROR_DESCRIPTION_EMPTY;
import static ru.practicum.shareit.exception.ItemValidationMessages.ERROR_NAME_EMPTY;

@Data
public class ItemUpdateDto {

    @Size(min = 1, message = ERROR_NAME_EMPTY)
    private String name;

    @Size(min = 1, message = ERROR_DESCRIPTION_EMPTY)
    private String description;

    private Boolean available;
}