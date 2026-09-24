package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
    select b from Booking b
    join fetch b.item
    join fetch b.booker
    where b.booker.id = :bookerId
    order by b.start desc
""")
    List<Booking> findByBookerIdOrderByStartDesc(@Param("bookerId") Long bookerId);

    @Query("""
    select b from Booking b
    join fetch b.item
    join fetch b.booker
    where b.item.owner.id = :ownerId
    order by b.start desc
""")
    List<Booking> findByItemOwnerIdOrderByStartDesc(@Param("ownerId") Long ownerId);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId,
                                                           Long itemId,
                                                           Booking.Status status,
                                                           LocalDateTime now);

    @Query("""
        select b from Booking b
        join fetch b.item i
        join fetch b.booker
        where i.id in :itemIds and b.status = :status
        order by b.end desc
    """)
    List<Booking> findByItemIdInAndStatusOrderByEndDesc(@Param("itemIds") List<Long> itemIds,
                                                        @Param("status") Booking.Status status);

    @Query("""
        select b from Booking b
        join fetch b.item i
        join fetch b.booker
        where i.id in :itemIds and b.status = :status
        order by b.start asc
    """)
    List<Booking> findByItemIdInAndStatusOrderByStartAsc(@Param("itemIds") List<Long> itemIds,
                                                         @Param("status") Booking.Status status);

}