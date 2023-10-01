package ru.practicum.shareit.user.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.exceptions.ConflictEmailException;
import ru.practicum.shareit.util.exceptions.ObjectNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UserStorage in memory implementation
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class UserInMemoryStorage implements UserStorage {

    private Long nextId = 1L;
    private final Map<Long, User> users;
    private final Map<Long, Long> items;

    /**
     * create User
     *
     * @param user User object to register
     * @return registered User object
     */
    @Override
    public User create(User user) {

        User userWithId = user.toBuilder().id(generateId()).build();
        saveUser(userWithId);
        log.info("Создан пользователь: {} ", userWithId);
        return userWithId;
    }

    /**
     * get User object
     *
     * @param userId user's id
     * @return User object
     */
    @Override
    public User getById(Long userId) {
        if (users.containsKey(userId)) {
            User user = users.get(userId);
            log.info("Найден пользователь с id: {}, {} ", userId, user);
            return user;
        } else {
            log.info("Не найден пользователь с id: {} ", userId);
            throw new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
        }
    }

    /**
     * update User object
     *
     * @param user User object with properties to update
     * @return updated User object
     */
    @Override
    public User update(User user) {
        saveUser(user);
        log.info("Обновлены данные пользователя с id: {}, {}", user.getId(), user);
        return user;
    }

    /**
     * delete User object
     *
     * @param userId user's id
     * @return if delete operation was executed
     */
    @Override
    public boolean delete(Long userId) {
        if (users.containsKey(userId)) {
            users.remove(userId);
            log.info("Удален пользователь с id: {} ", userId);
            return true;
        }
        return false;
    }

    /**
     * get all users
     *
     * @return list of User objects
     */
    @Override
    public List<User> getList() {
        List<User> list = new ArrayList<>(users.values());
        logResultList(list);
        return list;
    }


    /**
     * get all items of a specific user
     *
     * @param userId user's id
     * @return list of item's id
     */
    @Override
    public List<Long> getItemsId(Long userId) {
        List<Long> itemsIds = items.entrySet().stream().filter(entry -> entry.getValue().equals(userId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        log.info("Запрошена информация о списке вещей пользователя с id {}", userId);
        return itemsIds;
    }

    /**
     * save info about item's owner into the map.of(itemId, ownerId)
     *
     * @param ownerId owner's id
     * @param itemId  item's id
     */
    @Override
    public void saveItem(Long ownerId, Long itemId) {
        log.info("Сохранен id владельца вещи с id: {} : {} ", itemId, ownerId);
        items.put(itemId, ownerId);
    }

    /**
     * delete info about deleted item owner from the map.of(itemId, ownerId)
     *
     * @param itemId item's id
     */
    @Override
    public void deleteItem(Long itemId) {
        log.info("Удалена информация о владельце вещи с id {}", itemId);
        items.remove(itemId);
    }

    /**
     * checks the existence of the registered same email address for another user
     *
     * @param email  email
     * @param userId user's id
     */
    public void checkEmailExists(String email, Long userId) {
        Optional<User> userWithSameEmail = users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findAny();
        if (userId == null) {
            if (userWithSameEmail.isPresent()) {
                log.info("Email {} уже зарегистрирован в базе.", email);
                throw new ConflictEmailException(String.format("Email %s уже зарегистрирован в базе."
                        , email));
            }
        } else {
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(userId)) {
                log.info("Email {} уже зарегистрирован в базе.", email);
                throw new ConflictEmailException(String.format("Email %s уже зарегистрирован в базе."
                        , email));
            }
        }
    }

    /**
     * checks if user is registered in Storage
     *
     * @param userId user's id
     */
    public void checkUserIdExists(Long userId) {

        if (!users.containsKey(userId)) {
            log.info("Ошибка при проверке id пользователя, id: {} ", userId);
            throw new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
        }
    }

    /**
     * check if the item belongs to a specific user
     *
     * @param userId user's id
     * @param itemId item's id
     */
    public void checkOwner(Long userId, Long itemId) {

        if (!getItemsId(userId).contains(itemId)) {
            throw new ObjectNotFoundException("У пользователя нет прав доступа к этой вещи");
        }
    }

    /**
     * generate next id
     *
     * @return next id
     */
    private Long generateId() {
        return nextId++;
    }

    /**
     * save new or updated Object into map for storage
     *
     * @param user User object
     */
    private void saveUser(User user) {
        users.put(user.getId(), user);
    }

    /**
     * log list of objects in pretty format
     *
     * @param list list of users
     */
    private void logResultList(List<User> list) {

        String result = list.stream()
                .map(User::toString)
                .collect(Collectors.joining(", "));

        log.info("Список пользователей по запросу: {}", result);

    }


}
