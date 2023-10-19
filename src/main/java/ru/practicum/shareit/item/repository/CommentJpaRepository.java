package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

/**
 * JpaRepository for comments
 */
@Repository
public interface CommentJpaRepository extends JpaRepository<Comment, Long> {
    /**
     * find if exists all comments by itemId
     *
     * @param itemId item's id
     * @return list of comments by itemId or empty list
     */
    List<Comment> findAllByItemId(Long itemId);

    /**
     * find if exists all comments by itemIds
     *
     * @param items items
     * @return list of comments by itemId or empty list
     */
    List<Comment> findAllByItemIn(List<Item> items);
}
