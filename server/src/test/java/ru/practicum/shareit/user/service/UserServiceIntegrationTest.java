package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.exceptions.ConflictEmailException;
import ru.practicum.shareit.util.exceptions.ObjectNotFoundException;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService Integration tests
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserServiceIntegrationTest {

    @Autowired
    UserService userService;

    /**
     * test create method
     */
    @Test
    public void shouldCreateUser_andGetByExistingId() {

        //create UserDto
        UserDto userDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //map userDto into User
        User user = UserMapper.toUser(userDto);

        //create savedUser
        Long userId = 1L;
        User savedUser = user.toBuilder()
                .id(userId)
                .build();

        //create expectedUserDto
        UserDto expectedUserDto = UserMapper.toUserDto(savedUser);

        //invoke created tested method
        UserDto result = userService.create(userDto);

        //check result
        assertEquals(result, expectedUserDto);

    }

    /**
     * test getById method
     * should get user
     */
    @Test
    public void shouldGetUserByExistingId() {

        //create UserDto
        UserDto userDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //map userDto into User
        User user = UserMapper.toUser(userDto);

        //create User
        Long userId = 1L;
        User savedUser = user.toBuilder()
                .id(userId)
                .build();

        //create expectedUserDto
        UserDto expectedUserDto = UserMapper.toUserDto(savedUser);

        //save user
        userService.create(userDto);

        //invoke tested method
        UserDto result = userService.getById(userId);

        //check result
        assertEquals(result, expectedUserDto);

    }

    /**
     * test getById method
     * should throw ObjectFoundException if user not found
     */
    @Test
    public void shouldThrowExceptionIfUserNotFound() {

        //create userId
        Long userId = 1L;

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> userService.getById(userId),
                String.format("Пользователя с id %d не существует", userId));

    }

    /**
     * test update method
     * when user exists
     * when all valid fields to update are valid
     * should update user and return result
     */
    @Test
    public void shouldUpdateValidUserFields() {

        //create userDto with valid fields to update
        UserDto validNew = UserDto.builder()
                .id(3L)
                .name("NewName")
                .email("New@Email")
                .build();

        //create userDto
        Long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //save user
        UserDto savedUserDto = userService.create(userDto);
        User savedUser = UserMapper.toUser(savedUserDto);

        //create updated User
        User updatedUser = savedUser.toBuilder()
                .name(validNew.getName())
                .email(validNew.getEmail())
                .build();

        //create expected updated UserDto
        UserDto expectedUserDto = UserMapper.toUserDto(updatedUser);

        //invoke tested method
        UserDto result = userService.update(validNew, userId);

        //check result
        assertEquals(result, expectedUserDto);
        assertThat(expectedUserDto)
                .hasFieldOrPropertyWithValue("id", userId)
                .hasFieldOrPropertyWithValue("name", "NewName")
                .hasFieldOrPropertyWithValue("email", "New@Email");

    }

    /**
     * test update method
     * when user does not exist
     * should throw ObjectNotFoundException
     */
    @Test
    public void shouldFailUpdateIfUserNotFound() {

        //create userId
        Long userId = 1L;

        //create userDto with valid fields to update
        UserDto validNew = UserDto.builder()
                .id(3L)
                .name("NewName")
                .email("New@Email")
                .build();

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> userService.update(validNew, userId),
                String.format("Пользователя с id %d не существует", userId));

    }

    /**
     * test update method
     * when user found
     * when update valid name field
     * should ignore email is null
     * should ignore new id field
     * should update and return user with updated name
     */
    @Test
    public void shouldUpdateOnlyUserName() {

        //create UserDto to update valid name only
        UserDto validNew = UserDto.builder()
                .id(3L)
                .name("NewName")
                .build();

        //create userDto
        Long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //save user
        UserDto savedUserDto = userService.create(userDto);
        User savedUser = UserMapper.toUser(savedUserDto);

        //create updated User
        User updatedUser = savedUser.toBuilder()
                .name(validNew.getName())
                .build();

        //create expected updated UserDto
        UserDto expectedUserDto = UserMapper.toUserDto(updatedUser);

        //invoke tested method
        UserDto result = userService.update(validNew, userId);

        //check result
        assertEquals(result, expectedUserDto);
        assertThat(expectedUserDto)
                .hasFieldOrPropertyWithValue("id", userId)
                .hasFieldOrPropertyWithValue("name", "NewName")
                .hasFieldOrPropertyWithValue("email", "Olga@yandex.ru");

    }

    /**
     * test update method
     * when user exists
     * when update valid email field
     * should ignore name is null
     * should ignore new id field
     * should update and return user with updated email
     */
    @Test
    public void shouldUpdateOnlyUserEmail() {

        //create UserDto to update valid email only
        UserDto validNew = UserDto.builder()
                .id(3L)
                .email("new@mail.ru")
                .build();

        //create userDto
        Long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //save user
        UserDto savedUserDto = userService.create(userDto);
        User savedUser = UserMapper.toUser(savedUserDto);

        //create updated User
        User updatedUser = savedUser.toBuilder()
                .email(validNew.getEmail())
                .build();

        //create expected updated UserDto
        UserDto expectedUserDto = UserMapper.toUserDto(updatedUser);

        //invoke tested method
        UserDto result = userService.update(validNew, userId);

        //check result
        assertEquals(result, expectedUserDto);
        assertThat(expectedUserDto)
                .hasFieldOrPropertyWithValue("id", userId)
                .hasFieldOrPropertyWithValue("name", "Olga")
                .hasFieldOrPropertyWithValue("email", "new@mail.ru");

    }

    /**
     * test update method
     * when user exists
     * when update invalid email field
     * should ignore name is null
     * should ignore invalid email without @ symbol
     * should ignore new id field
     * should return non-updated user
     */
    @Test
    public void shouldReturnNonUpdatedUser() {

        //create UserDto to update invalid email
        UserDto invalidNew = UserDto.builder()
                .id(3L)
                .email("NewEmail")
                .build();

        //create userDto
        Long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //save user
        UserDto savedUserDto = userService.create(userDto);
        User savedUser = UserMapper.toUser(savedUserDto);

        //create expected updated UserDto
        UserDto expectedUserDto = UserMapper.toUserDto(savedUser);

        //invoke tested method
        UserDto result = userService.update(invalidNew, userId);

        //check result
        assertEquals(result, expectedUserDto);
        assertThat(expectedUserDto)
                .hasFieldOrPropertyWithValue("id", userId)
                .hasFieldOrPropertyWithValue("name", "Olga")
                .hasFieldOrPropertyWithValue("email", "Olga@yandex.ru");

    }

    /**
     * test update method
     * when user exists
     * when update email with already existed email
     * should throw ConflictEmailException
     */
    @Test
    public void shouldFailUpdateWithOtherUserEmail() {

        //create UserDto to update other email
        UserDto invalidNew = UserDto.builder()
                .id(3L)
                .email("Olga@yandex.ru")
                .build();

        //create userDto
        UserDto user = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        Long user2Id = 2L;
        UserDto userWithSameEmailDto = UserDto.builder()
                .name("Olga Smith")
                .email("OlgaS@yandex.ru")
                .build();

        //save users
        userService.create(user);
        userService.create(userWithSameEmailDto);

        //invoke tested method to check throws
        assertThrows(ConflictEmailException.class,
                () -> userService.update(invalidNew, user2Id),
                String.format("Email %s уже зарегистрирован в базе.", invalidNew.getEmail()));
    }

    /**
     * test delete method
     * when user exists
     * should delete user by id
     */
    @Test
    public void shouldDeleteUser() {

        //create userDto
        Long userId = 1L;
        UserDto user = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //save user
        userService.create(user);

        //invoke tested method
        userService.deleteById(userId);

        //check user is not found after deleting
        assertThrows(ObjectNotFoundException.class,
                () -> userService.getById(userId),
                String.format("Пользователя с id %d не существует", userId));

    }


    /**
     * test findAll method
     * should return list of users or empty list
     */
    @Test
    public void shouldFindAllUsers() {

        //create users
        Long user1Id = 1L;
        UserDto userDto1 = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        UserDto savedUser1 = userService.create(userDto1);
        Long user2Id = 1L;
        UserDto userDto2 = UserDto.builder()
                .name("Name")
                .email("name@google.com")
                .build();
        UserDto savedUser2 = userService.create(userDto2);

        List<UserDto> users = List.of(savedUser1, savedUser2);

        //invoke tested method
        List<UserDto> result = userService.findAll();

        //check result
        assertEquals(result, users);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getId(), user1Id);
        assertEquals(result.get(0).getName(), "Olga");
        assertEquals(result.get(0).getEmail(), "Olga@yandex.ru");
        assertEquals(result.get(0).getId(), user2Id);
        assertEquals(result.get(1).getName(), "Name");
        assertEquals(result.get(1).getEmail(), "name@google.com");
    }


}
