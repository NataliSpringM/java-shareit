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
     * to add user's data (save and assign identity)
     *
     * @param userDto user to save and registered
     * @return user with assigned id
     */
    UserDto create(UserDto userDto);

    /**
     * get user by id
     *
     * @param userId user's id
     * @return user
     */
    UserDto getById(Long userId);

    /**
     * update user's properties
     *
     * @param userId  user's id
     * @param userDto user to update
     * @return updated user
     */
    UserDto update(UserDto userDto, Long userId);

    /**
     * delete user by id
     *
     * @param userId user's id
     */
    void deleteById(Long userId);

    /**
     * get all users
     *
     * @return list of users
     */
    List<UserDto> findAll();

}
