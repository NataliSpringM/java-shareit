package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.exceptions.ConflictEmailException;
import ru.practicum.shareit.util.exceptions.ObjectNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl tests
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @InjectMocks
    UserServiceImpl userService;
    @Mock
    UserRepository userRepository;

    /**
     * test create method
     * should invoke save in repository
     * should return result of save method: saved user with assigned id
     */
    @Test
    public void create_returnSavedUser() {

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

        //mock repository answer
        when(userRepository.save(user)).thenReturn(savedUser);

        //invoke tested method
        UserDto result = userService.create(userDto);

        //verify invokes
        verify(userRepository).save(user);

        //check result
        assertEquals(result, expectedUserDto);
    }

    /**
     * test getById method
     * should invoke findById method in repository
     * should return user if it exists in repository
     */
    @Test
    public void getById_whenUserFound_thenReturnUser() {

        //create user
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create expectedUserDto
        UserDto expectedUserDto = UserMapper.toUserDto(user);

        //mock repository answer
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //invoke tested method
        UserDto result = userService.getById(userId);

        //verify invokes
        verify(userRepository).findById(userId);

        //check result
        assertEquals(result, expectedUserDto);
    }

    /**
     * test getById method
     * should invoke findById method in repository
     * should throw ObjectNotFoundException if user doesn't exist
     */
    @Test
    public void getById_whenUserNotFound_thenThrowObjectNotFound() {

        //create userId
        Long userId = 1L;

        //mock repository answer
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> userService.getById(userId),
                String.format("Пользователя с id %d не существует", userId));

        //verify invokes
        verify(userRepository).findById(userId);
    }

    /**
     * test update method
     * when user exists
     * when all valid fields to update are valid
     * should ignore new id field
     * should invoke save method in repository
     * should return updated user
     */
    @Test
    public void update_whenUserExists_andWhenFieldsToUpdateAreValid_ignoreIdField_returnUpdatedUser() {

        //create user
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //mock repository answer
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //create userDto with valid fields to update
        UserDto validNew = UserDto.builder()
                .id(3L)
                .name("NewName")
                .email("New@Email")
                .build();

        //create updated User
        User updatedUser = user.toBuilder()
                .name(validNew.getName())
                .email(validNew.getEmail())
                .build();

        //create expected updated UserDto
        UserDto expectedUserDto = UserMapper.toUserDto(updatedUser);

        //invoke tested method
        UserDto result = userService.update(validNew, userId);

        //verify invokes
        verify(userRepository).save(updatedUser);

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
     * should not invoke other methods after checking
     */
    @Test
    public void update_whenUserDoesNotExists_thenThrowObjectNotFound() {

        //create userId
        Long userId = 1L;

        //create userDto with valid fields to update
        UserDto validNew = UserDto.builder()
                .id(3L)
                .name("NewName")
                .email("New@Email")
                .build();

        //mock repository answer
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> userService.update(validNew, userId),
                String.format("Пользователя с id %d не существует", userId));

        //verify invokes
        verify(userRepository, only()).findById(userId);
        verifyNoMoreInteractions(userRepository);

    }

    /**
     * test update method
     * when user found
     * when update valid name field
     * should ignore email is null
     * should ignore new id field
     * should invoke save method in repository and returned user with updated name
     */
    @Test
    public void update_whenUserFound_nameToUpdateIsValid_emailIsNull_ignoreIdField_returnUpdatedUser() {

        //create user
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create UserDto to update valid name only
        UserDto validNew = UserDto.builder()
                .id(3L)
                .name("NewName")
                .build();

        //create updated User
        User updatedUser = user.toBuilder()
                .name(validNew.getName())
                .build();

        //create expected updated UserDto
        UserDto expectedUserDto = UserMapper.toUserDto(updatedUser);

        //mock repository answer
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //invoke tested method
        UserDto result = userService.update(validNew, userId);

        //verify invokes
        verify(userRepository).save(updatedUser);

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
     * should invoke save in repository and return user with updated email
     */
    @Test
    public void update_whenUserExists_emailIsValid_nameIsNull_ignoreIdField_returnUpdatedUser() {

        //create user
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create UserDto to update valid email only
        UserDto validNew = UserDto.builder()
                .id(3L)
                .email("new@mail.ru")
                .build();

        //create updated User
        User updatedUser = user.toBuilder()
                .email(validNew.getEmail())
                .build();

        //create expected updated UserDto
        UserDto expectedUserDto = UserMapper.toUserDto(updatedUser);

        //mock repository answer
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //invoke tested method
        UserDto result = userService.update(validNew, userId);

        //verify invokes
        verify(userRepository).save(updatedUser);

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
    public void update_whenUserExists_emailToUpdateIsInvalid_ignoreIdField_returnUpdatedUser() {

        //create user
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create UserDto to update invalid email
        UserDto invalidNew = UserDto.builder()
                .id(3L)
                .email("NewEmail")
                .build();

        //create updated User

        //create expected updated UserDto
        UserDto expectedUserDto = UserMapper.toUserDto(user);

        //mock repository answer
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //invoke tested method
        UserDto result = userService.update(invalidNew, userId);

        //verify invokes
        verify(userRepository).save(user);

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
     * should not invoke method save
     */
    @Test
    public void update_whenUserExists_emailToUpdateIsNotAvailable_thenThrowConflictEmail_doesNotInvokeSave() {

        //create user
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create User with same email
        User userWithSameEmail = User.builder()
                .name("Olga Smith")
                .email("Olga@yandex.ru")
                .build();

        //create UserDto to update other email
        UserDto invalidNew = UserDto.builder()
                .id(3L)
                .email("Olga@yandex.ru")
                .build();

        //mock repository answer
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmailAndIdIsNot(invalidNew.getEmail(), userId))
                .thenReturn(Optional.of(userWithSameEmail));

        //invoke tested method to check throws
        assertThrows(ConflictEmailException.class,
                () -> userService.update(invalidNew, userId),
                String.format("Email %s уже зарегистрирован в базе.", invalidNew.getEmail()));

        //verify invokes
        verify(userRepository).findById(userId);
        verify(userRepository).findByEmailAndIdIsNot(invalidNew.getEmail(), userId);
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);
    }

    /**
     * test delete method
     * should invoke method deleteById in repository
     */
    @Test
    public void delete_whenUserExists_invokeRepository() {

        //create userId
        Long userId = 1L;

        //invoke tested method
        userService.deleteById(userId);

        //verify invokes
        verify(userRepository).deleteById(userId);
    }


    /**
     * test findAll method
     * should invoke findAll in repository and return result: list of users or empty list
     */
    @Test
    public void findAll_invokeRepository_returnListOfUsers() {

        //create users
        Long user1Id = 1L;
        User user1 = User.builder()
                .id(user1Id)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        Long user2Id = 1L;
        User user2 = User.builder()
                .id(user2Id)
                .name("Name")
                .email("Email")
                .build();
        List<UserDto> users = UserMapper.toUserDtoList(List.of(user1, user2));

        //mock repository answer
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        //invoke tested method
        List<UserDto> result = userService.findAll();

        //verify invokes
        verify(userRepository).findAll();

        //check result
        assertEquals(result, users);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getName(), "Olga");
        assertEquals(result.get(1).getName(), "Name");
    }

}
