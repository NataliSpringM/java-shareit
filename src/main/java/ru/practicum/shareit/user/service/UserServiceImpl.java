package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.util.Validation;

import java.util.List;

/**
 * UserService implementation
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    /**
     * create UserDto
     *
     * @param userDto UserDto object to register
     * @return registered UserDto object
     */
    @Override
    public UserDto create(UserDto userDto) {

        userStorage.checkEmailExists(userDto.getEmail(), null);
        User user = UserMapper.toUser(userDto);
        User userWithId = userStorage.create(user);
        return UserMapper.toUserDto(userWithId);
    }

    /**
     * get UserDto object
     *
     * @param userId user's id
     * @return ItemDto object
     */
    @Override
    public UserDto getById(Long userId) {
        return UserMapper.toUserDto(userStorage.getById(userId));
    }

    /**
     * update UserDto object
     *
     * @param userId  user's id
     * @param userDto UserDto object with properties to update
     * @return updated UserDto object
     */
    @Override
    public UserDto update(UserDto userDto, Long userId) {
        userStorage.checkUserIdExists(userId);
        User user = updateValidFields(userDto, userId);
        userStorage.update(user);
        return UserMapper.toUserDto(user);
    }

    /**
     * delete UserDto object
     *
     * @param userId user's id
     */
    @Override
    public boolean delete(Long userId) {
        return userStorage.delete(userId);
    }

    /**
     * get all UserDto objects
     *
     * @return list of UserDto objects
     */
    @Override
    public List<UserDto> getList() {
        return UserMapper.toUserDtoList(userStorage.getList());
    }

    /**
     * update valid fields
     *
     * @param userDto properties to check and update
     * @param userId  user's id
     * @return updated Item object
     */
    private User updateValidFields(UserDto userDto, Long userId) {

        User user = userStorage.getById(userId);
        String newEmail = userDto.getEmail();
        String newName = userDto.getName();
        if (Validation.stringIsNotNullOrBlank(newEmail) && Validation.validEmail(newEmail)) {
            userStorage.checkEmailExists(newEmail, userId);
            user = user.toBuilder().email(userDto.getEmail()).build();
        }
        if (Validation.stringIsNotNullOrBlank(newName)) {
            user = user.toBuilder().name(newName).build();
        }
        return user;
    }
}
