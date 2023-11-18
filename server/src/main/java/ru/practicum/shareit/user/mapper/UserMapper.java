package ru.practicum.shareit.user.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * map User, UserDto objects into each other
 */
@Component
public class UserMapper {
    /**
     * map User object into UserDto object
     *
     * @param user User object
     * @return UserDto object
     */
    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    /**
     * map UserDto object into User object
     *
     * @param userDto UserDto object
     * @return User object
     */
    public static User toUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();

    }

    /**
     * map List of User objects into List of UserDto objects
     *
     * @param users list of User objects
     * @return List of UserDto objects
     */
    public static List<UserDto> toUserDtoList(List<User> users) {
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

}
