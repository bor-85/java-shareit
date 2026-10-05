package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Booking.Status status;
    private BookerShortDto booker; // Изменили тип обратно на объект с именем
    private ItemShortDto item;

    @Data
    public static class BookerShortDto {
        private Long id;
        private String name;
    }

    @Data
    public static class ItemShortDto {
        private Long id;
        private String name;
    }
}
