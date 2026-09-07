package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static ru.practicum.shareit.exception.ItemValidationMessages.*;

@Data
public class ItemDto {
    private Long id;

    @NotBlank(message = ERROR_NAME_EMPTY)
    private String name;

    @NotBlank(message = ERROR_DESCRIPTION_EMPTY)
    private String description;

    @NotNull(message = ERROR_AVAILABLE_EMPTY)
    private boolean available;

    private Long requestId;
}