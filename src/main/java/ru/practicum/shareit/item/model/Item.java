package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static ru.practicum.shareit.exception.ItemValidationMessages.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private Long id;

    @NotBlank(message = ERROR_NAME_EMPTY)
    private String name;

    @NotBlank(message = ERROR_DESCRIPTION_EMPTY)
    private String description;

    @NotNull(message = ERROR_AVAILABLE_EMPTY)
    private boolean available;

    private User owner;
    private ItemRequest request;
}