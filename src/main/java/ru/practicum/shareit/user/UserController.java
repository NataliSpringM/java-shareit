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
 * обработка запросов HTTP-клиентов на добавление, обновление, получение
 * информации о пользователях по адресу <a href="http://localhost:8080/users">...</a>
 */

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * обработка POST-запроса на добавление данных пользователя
     *
     * @param userDto объект UserDto
     * @return userDto объект
     */
    @PostMapping()
    @Validated({Create.class})
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("POST-request to create User: {}", userDto);
        return userService.create(userDto);
    }

    /**
     * processing a GET request to get a user by id
     *
     * @param id id пользователя
     * @return объект UserDto
     */
    @GetMapping("{id}")
    public UserDto getById(@PathVariable Long id) {
        log.info("GET-request to get a user by id: {}", id);
        return userService.getById(id);

    }

    /**
     * processing PATCH-request to patch a user's properties
     *
     * @param userDto объект UserDto
     * @return обновленный объект User
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
     * @param id id пользователя
     */
    @DeleteMapping(value = "/{id}")
    public void delete(@Valid @PathVariable Long id) {
        log.info("DELETE-request to delete user by id: {}", id);
        userService.deleteById(id);

    }

    /**
     * обработка GET-запроса на получение списка пользователей
     *
     * @return список пользователей
     */
    @GetMapping()
    public List<UserDto> getList() {
        log.info("GET-request to get all users");
        return userService.findAll();

    }


}


