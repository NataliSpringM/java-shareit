package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.List;

/**
 * UserStorage interface
 */
@Component
public interface UserStorage {
    /**
     * create User
     *
     * @param user User object to register
     * @return registered User object
     */
    User create(User user);

    /**
     * get User object
     *
     * @param userId user's id
     * @return User object
     */
    User getById(Long userId);

    /**
     * update User object
     *
     * @param user User object with properties to update
     * @return updated User object
     */
    User update(User user);

    /**
     * delete User object
     *
     * @param userId user's id
     * @return if delete operation was executed
     */
    boolean delete(Long userId);

    /**
     * get all users
     *
     * @return list of User objects
     */
    List<User> getList();

    /**
     * get all items of a specific user
     *
     * @param userId user's id
     * @return list of item's id
     */
    List<Long> getItemsId(Long userId);

    /**
     * save info about item's owner into the map.of(itemId, ownerId)
     *
     * @param userId owner's id
     * @param itemId item's id
     */

    void saveItem(Long userId, Long itemId);

    /**
     * delete info about deleted item owner from the map.of(itemId, ownerId)
     *
     * @param itemId item's id
     */

    void deleteItem(Long itemId);

    /**
     * checks if user is registered in Storage
     *
     * @param userId user's id
     */
    void checkUserIdExists(Long userId);

    /**
     * checks the existence of the registered same email address for another user
     *
     * @param email  email
     * @param userId user's id
     */

    void checkEmailExists(String email, Long userId);

    /**
     * check if the item belongs to a specific user
     *
     * @param userId user's id
     * @param itemId item's id
     */
    void checkOwner(Long userId, Long itemId);
}
