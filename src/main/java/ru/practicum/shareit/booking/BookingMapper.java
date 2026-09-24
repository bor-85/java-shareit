package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

@Component
public class BookingMapper {

    public BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());

        if (booking.getBooker() != null) {
            BookingDto.BookerShortDto bookerDto = new BookingDto.BookerShortDto();
            bookerDto.setId(booking.getBooker().getId());
            bookerDto.setName(booking.getBooker().getName());
            dto.setBooker(bookerDto);
        }

        if (booking.getItem() != null) {
            BookingDto.ItemShortDto itemDto = new BookingDto.ItemShortDto();
            itemDto.setId(booking.getItem().getId());
            itemDto.setName(booking.getItem().getName());
            dto.setItem(itemDto);
        }

        return dto;
    }
}