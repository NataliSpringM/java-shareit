package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;
import ru.practicum.shareit.util.Validation;
import ru.practicum.shareit.util.exceptions.ConflictEmailException;
import ru.practicum.shareit.util.exceptions.ObjectNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UserService implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserJpaRepository userJpaRepository;

    /**
     * to add user's data (save and assign identity)
     *
     * @param userDto user to save and register
     * @return user with assigned id
     */
    @Override
    public UserDto create(UserDto userDto) {

        User user = UserMapper.toUser(userDto);
        User userWithId = userJpaRepository.save(user);
        log.info("Создан пользователь: {} ", userWithId);
        return UserMapper.toUserDto(userWithId);
    }

    /**
     * get user by id
     * throw 404.NOT FOUND ObjectNotFoundException if user does not exist
     *
     * @param userId user's id
     * @return user
     */
    @Override
    public UserDto getById(Long userId) {

        User user = getUserByIdIfExists(userId);
        log.info("Найден пользователь с id: {}, {} ", userId, user);
        return UserMapper.toUserDto(user);
    }

    /**
     * update user's properties
     * throw 404.NOT FOUND ObjectNotFoundException if userId does not exist
     *
     * @param userId  user's id
     * @param userDto user to update
     * @return updated user
     */
    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long userId) {

        User user = updateValidFields(userDto, userId);
        userJpaRepository.save(user);
        log.info("Обновлены данные пользователя с id: {}, {}", user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    /**
     * delete user by id
     *
     * @param userId user's id
     */
    @Override
    @Transactional
    public void deleteById(Long userId) {
        if (userJpaRepository.existsById(userId)) {
            log.info("Удален пользователь с id: {}", userId);
            userJpaRepository.deleteById(userId);
        }
        log.info("Пользователь с id {} не найден", userId);
    }

    /**
     * get all users
     *
     * @return list of users or empty list
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        List<User> users = userJpaRepository.findAll();
        List<UserDto> usersDto = UserMapper.toUserDtoList(users);
        logResultList(usersDto);
        return usersDto;
    }

    /**
     * update valid fields
     * throws  404.NOT FOUND ObjectNotFoundException if userId does not exist
     * throws ConflictEmailException if try updating registered by other user email
     *
     * @param userDto properties to check and update
     * @param userId  user's id
     * @return updated Item object
     */
    private User updateValidFields(UserDto userDto, Long userId) {

        User user = getUserByIdIfExists(userId);
        String newEmail = userDto.getEmail();
        String newName = userDto.getName();
        if (Validation.stringIsNotNullOrBlank(newEmail) && Validation.validEmail(newEmail)) {
            checkEmailExists(newEmail, userId);
            user = user.toBuilder().email(userDto.getEmail()).build();
        }
        if (Validation.stringIsNotNullOrBlank(newName)) {
            user = user.toBuilder().name(newName).build();
        }
        return user;
    }

    /**
     * checks the existence of the registered same email address for another user
     * throws ConflictEmailException
     *
     * @param email  email
     * @param userId user's id
     */
    private void checkEmailExists(String email, Long userId) {

        Optional<User> userWithSameEmail = userJpaRepository.findAll().stream()
                .filter(user -> user.getEmail().equals(email) && !user.getId().equals(userId))
                .findAny();

        if (userWithSameEmail.isPresent()) {
            log.info("Email {} уже зарегистрирован в базе.", email);
            throw new ConflictEmailException(String.format("Email %s уже зарегистрирован в базе.", email));
        }
    }

    /**
     * get User if exists throw 404.NOT FOUND ObjectNotFoundException if isn't
     *
     * @param userId user's id
     * @return User object
     */
    private User getUserByIdIfExists(Long userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() ->
                        new ObjectNotFoundException(String.format("Пользователя с id %d не существует", userId)));
    }

    /**
     * log list of objects in pretty format
     *
     * @param users list of users
     */
    private void logResultList(List<UserDto> users) {
        String result = users.stream()
                .map(UserDto::toString)
                .collect(Collectors.joining(", "));
        log.info("Список пользователей по запросу: {}", result);

    }

}

