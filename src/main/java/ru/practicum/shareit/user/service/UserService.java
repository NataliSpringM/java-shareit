package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

/**
 * UserService interface
 */
@Component
public interface UserService {
    /**
     * create UserDto
     *
     * @param userDto UserDto object to register
     * @return registered UserDto object
     */
    UserDto create(UserDto userDto);

    /**
     * get UserDto object
     *
     * @param userId user's id
     * @return ItemDto object
     */
    UserDto getById(Long userId);

    /**
     * update UserDto object
     *
     * @param userId  user's id
     * @param userDto UserDto object with properties to update
     * @return updated UserDto object
     */
    UserDto update(UserDto userDto, Long userId);

    /**
     * delete User object
     *
     * @param userId user's id
     */
    void deleteById(Long userId);

    /**
     * get all UserDto objects
     *
     * @return list of UserDto objects
     */
    List<UserDto> findAll();

}
