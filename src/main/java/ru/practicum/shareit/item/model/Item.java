package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import static ru.practicum.shareit.exception.ItemValidationMessages.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = ERROR_NAME_EMPTY)
    @Column(nullable = false)
    private String name;

    @NotBlank(message = ERROR_DESCRIPTION_EMPTY)
    @Column(nullable = false, length = 512)
    private String description;

    @Column(nullable = false)
    private boolean available;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private ItemRequest request;
}