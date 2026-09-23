package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    Optional<Booking> findTopByItemIdAndStatusAndStartAfterOrderByStartAsc(Long itemId,
                                                                           Booking.Status status,
                                                                           LocalDateTime now);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId,
                                                           Long itemId,
                                                           Booking.Status status,
                                                           LocalDateTime now);

    Optional<Booking> findTopByItemIdAndStatusAndEndBeforeOrderByEndDesc(Long itemId,
                                                                         Booking.Status status,
                                                                         LocalDateTime now);
}