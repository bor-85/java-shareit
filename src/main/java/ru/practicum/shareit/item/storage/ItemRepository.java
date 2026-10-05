package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("""
        select i from Item i
        left join fetch i.request
        where i.owner.id = :ownerId
    """)
    List<Item> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("""
        select i from Item i
        left join fetch i.request
        where i.available = true
          and (
            lower(i.name) like lower(concat('%', :text, '%'))
            or lower(i.description) like lower(concat('%', :text, '%'))
          )
    """)
    List<Item> searchAvailable(@Param("text") String text);
}