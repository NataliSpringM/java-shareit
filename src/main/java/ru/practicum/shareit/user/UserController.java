package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
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
public class UserController {

    private final UserService userService;

    /**
     * обработка POST-запроса на добавление данных пользователя
     *
     * @param userDto объект UserDto
     * @return объект  //TODO
     */
    @PostMapping()
    @Validated({Create.class})
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        System.out.println("CONTROLLER CREATE");
        return userService.create(userDto);
    }

    /**
     * обработка GET-запроса на получение пользователя по id
     *
     * @param id id пользователя
     * @return объект UserDto
     */
    @GetMapping("{id}")
    public UserDto getById(@PathVariable Long id) {

        return userService.getById(id);

    }

    /**
     * обработка PATCH-запроса на обновление данных пользователя
     *
     * @param userDto объект UserDto
     * @return обновленный объект User
     */
    @PatchMapping("/{id}")
    @Validated({Update.class})
    public UserDto update(@Valid @RequestBody UserDto userDto,
                          @PathVariable("id") Long id) {

        return userService.update(userDto, id);

    }

    /**
     * обработка DELETE-запроса
     *
     * @param id id пользователя
     * @return подтверждение удаления
     */
    @DeleteMapping(value = "/{id}")
    public boolean delete(@Valid @PathVariable Long id) {
        return userService.delete(id);

    }


    /**
     * обработка GET-запроса на получение списка пользователей
     *
     * @return список пользователей
     */
    @GetMapping()
    public List<UserDto> getList() {
        return userService.getList();

    }


}


