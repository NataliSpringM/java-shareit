package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.util.groups.Create;
import ru.practicum.shareit.util.groups.Update;

import javax.validation.Valid;

import static ru.practicum.shareit.util.constants.Constants.USERS_PATH;
import static ru.practicum.shareit.util.constants.Constants.USER_ID_PATH_VARIABLE;

/**
 * processing HTTP-requests to "/users" end-point to add, update and get users' data
 */

@Controller
@RequestMapping(USERS_PATH)
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserClient userClient;

    /**
     * POST-request processing to add user's data (save and assign identity)
     *
     * @param userDto user's data
     * @return user with assigned id
     */
    @PostMapping
    @Validated({Create.class})
    public ResponseEntity<Object> addUser(@Valid @RequestBody UserDto userDto) {
        log.info("New user: {}", userDto);
        return userClient.addUser(userDto);
    }

    /**
     * processing GET-request to get a user by id
     *
     * @param id user's id
     * @return user
     */
    @GetMapping(USER_ID_PATH_VARIABLE)
    public ResponseEntity<Object> getById(@PathVariable Long id) {
        log.info("Get a user by id: {}", id);
        return userClient.getUserById(id);

    }

    /**
     * processing PATCH-request to patch a user's properties
     *
     * @param userDto user to update
     * @return updated user
     */
    @PatchMapping(USER_ID_PATH_VARIABLE)
    @Validated({Update.class})
    public ResponseEntity<Object> update(@Valid @RequestBody UserDto userDto,
                                         @PathVariable Long id) {
        log.info("Updating user by id: {}, data to update: {}", id, userDto);
        return userClient.updateUser(userDto, id);

    }

    /**
     * processing DELETE-request to delete a user by id
     *
     * @param id user's id
     */
    @DeleteMapping(USER_ID_PATH_VARIABLE)
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        log.info("Deleting user by id: {}", id);
        return userClient.deleteUser(id);

    }

    /**
     * processing GET-request to get all users
     *
     * @return list of users
     */
    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Get all users' list");
        return userClient.getAllUsers();

    }

}
