package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.exception.GatewayExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(GatewayExceptionHandler.class)
class BookingControllerGatewayTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void createBookingShouldReturnCreatedBooking() throws Exception {
        BookingCreateDto request = new BookingCreateDto();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusHours(1));
        request.setEnd(LocalDateTime.now().plusHours(2));

        BookingDto response = buildBookingDto(1L, BookingStatus.WAITING);

        when(bookingClient.create(eq(1L), any(BookingCreateDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(response));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(2))
                .andExpect(jsonPath("$.item.id").value(3));

        verify(bookingClient).create(eq(1L), any(BookingCreateDto.class));
    }

    @Test
    void createBookingShouldReturnBadRequestForInvalidBody() throws Exception {
        String json = """
                {
                  "itemId": null,
                  "start": null,
                  "end": null
                }
                """;

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void approveBookingShouldReturnBooking() throws Exception {
        BookingDto response = buildBookingDto(1L, BookingStatus.APPROVED);

        when(bookingClient.approve(1L, 10L, true))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(patch("/bookings/{bookingId}", 10L)
                        .header(USER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingClient).approve(1L, 10L, true);
    }

    @Test
    void getBookingByIdShouldReturnBooking() throws Exception {
        BookingDto response = buildBookingDto(1L, BookingStatus.WAITING);

        when(bookingClient.getById(1L, 1L))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingClient).getById(1L, 1L);
    }

    @Test
    void getBookingsShouldReturnList() throws Exception {
        BookingDto booking1 = buildBookingDto(1L, BookingStatus.WAITING);
        BookingDto booking2 = buildBookingDto(2L, BookingStatus.APPROVED);

        when(bookingClient.getBookings(1L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok(List.of(booking1, booking2)));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(bookingClient).getBookings(1L, BookingState.ALL);
    }

    @Test
    void getOwnerBookingsShouldReturnList() throws Exception {
        BookingDto booking = buildBookingDto(1L, BookingStatus.APPROVED);

        when(bookingClient.getOwnerBookings(1L, BookingState.CURRENT))
                .thenReturn(ResponseEntity.ok(List.of(booking)));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 1L)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bookingClient).getOwnerBookings(1L, BookingState.CURRENT);
    }

    @Test
    void getBookingsShouldReturnBadRequestForUnknownState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 1L)
                        .param("state", "BROKEN_STATE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: BROKEN_STATE"));

        verifyNoInteractions(bookingClient);
    }

    private BookingDto buildBookingDto(Long id, BookingStatus status) {
        BookingDto dto = new BookingDto();
        dto.setId(id);
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));
        dto.setStatus(status);

        BookingDto.BookerShortDto booker = new BookingDto.BookerShortDto();
        booker.setId(2L);
        booker.setName("Booker");
        dto.setBooker(booker);

        BookingDto.ItemShortDto item = new BookingDto.ItemShortDto();
        item.setId(3L);
        item.setName("Item");
        dto.setItem(item);

        return dto;
    }
}