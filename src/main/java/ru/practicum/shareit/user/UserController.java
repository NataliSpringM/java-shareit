package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.util.groups.Create;
import ru.practicum.shareit.util.groups.Update;

import javax.validation.Valid;
import java.util.List;

/**
 * Sprint add-controllers.
 * processing HTTP-requests to "/users" end-point to add, update and get users' data
 */

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * POST-request processing to add user's data (save and assign identity)
     *
     * @param userDto user's data
     * @return user with assigned id
     */
    @PostMapping()
    @Validated({Create.class})
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("POST-request to create User: {}", userDto);
        return userService.create(userDto);
    }

    /**
     * processing GET-request to get a user by id
     *
     * @param id user's id
     * @return user
     */
    @GetMapping("{id}")
    public UserDto getById(@PathVariable Long id) {
        log.info("GET-request to get a user by id: {}", id);
        return userService.getById(id);

    }

    /**
     * processing PATCH-request to patch a user's properties
     *
     * @param userDto user to update
     * @return updated user
     */
    @PatchMapping("/{id}")
    @Validated({Update.class})
    public UserDto update(@Valid @RequestBody UserDto userDto,
                          @PathVariable("id") Long id) {
        log.info("PATCH-request to update user by id: {}", id);
        return userService.update(userDto, id);

    }

    /**
     * processing DELETE-request to delete a user by id
     *
     * @param id user's id
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("DELETE-request to delete user by id: {}", id);
        userService.deleteById(id);

    }

    /**
     * processing GET-request to get all users
     *
     * @return list of users
     */
    @GetMapping()
    public List<UserDto> getList() {
        log.info("GET-request to get all users");
        return userService.findAll();

    }

}


