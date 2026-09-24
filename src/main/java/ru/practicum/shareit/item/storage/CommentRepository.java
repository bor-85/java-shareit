package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
    select c from Comment c
    join fetch c.item i
    join fetch c.author
    where i.id in :itemIds
    order by c.created desc
    """)
    List<Comment> findAllByItemIdsOrderByCreatedDesc(@Param("itemIds") List<Long> itemIds);

    @Query("""
    select c from Comment c
    join fetch c.author
    where c.item.id = :itemId
    order by c.created desc
    """)
    List<Comment> findByItemIdOrderByCreatedDesc(@Param("itemId") Long itemId);

}