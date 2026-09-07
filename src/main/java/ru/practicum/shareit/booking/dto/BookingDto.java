package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long item;
    private Long booker;
    private String status;
}
