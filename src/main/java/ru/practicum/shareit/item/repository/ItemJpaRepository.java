package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

/**
 * JpaRepository for items
 */
@Repository
public interface ItemJpaRepository extends JpaRepository<Item, Long> {

    /**
     * find if exists all items of a specific user by user's id
     *
     * @param userId user's id
     * @return list of items of a specific user or empty list
     */
    List<Item> findAllByOwnerId(Long userId);

    /**
     * search all available items, contained substring in name or description,
     *
     * @param text substring for search
     * @return list of ItemDto objects or empty list
     */
    @Query("select i from Item i " +
            "where i.available = true and (upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or upper(i.description) like upper(concat('%', ?1, '%')))")
    List<Item> searchItemsBySubstring(String text);
}
