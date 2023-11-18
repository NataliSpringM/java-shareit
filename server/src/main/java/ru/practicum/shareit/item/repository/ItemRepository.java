package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

/**
 * Item repository
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * find if exists all items of a specific user by user's id
     *
     * @param userId user's id
     * @return list of items of a specific user or empty list
     */
    List<Item> findAllByOwnerIdOrderById(Long userId);

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

    /**
     * get list of items by request's id
     *
     * @param requestId request's id
     * @return list of items by request's id
     */
    List<Item> findAllByRequestId(Long requestId);

    /**
     * get list of items by requests' list
     *
     * @param itemRequests list of the requests
     * @return list of items by requests' list
     */
    List<Item> findAllByRequestIn(List<ItemRequest> itemRequests);
}
