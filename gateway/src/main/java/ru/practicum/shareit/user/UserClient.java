package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.util.client.BaseClient;

import static ru.practicum.shareit.util.constants.Constants.*;

/**
 * creation requests to "/users" endpoint
 */
@Service
public class UserClient extends BaseClient {
    @Autowired
    public UserClient(@Value(API_SERVER_URL) String serverUrl, RestTemplateBuilder restTemplateBuilder) {
        super(
                restTemplateBuilder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + USERS_PATH))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    /**
     * create POST-request to add user's data (save and assign identity)
     *
     * @param userDto user's data
     * @return POST-request
     */
    public ResponseEntity<Object> addUser(UserDto userDto) {
        return post(EMPTY_PATH, userDto);
    }

    /**
     * create GET-request to get a user by id
     *
     * @param id user's id
     * @return GET-request
     */
    public ResponseEntity<Object> getUserById(Long id) {
        return get(SLASH_PATH + id, id);
    }

    /**
     * create PATCH-request to patch a user's properties
     *
     * @param userDto user to update
     * @return PATCH-request
     */
    public ResponseEntity<Object> updateUser(UserDto userDto, Long id) {
        return patch(SLASH_PATH + id, id, userDto);
    }

    /**
     * create DELETE-request to delete a user by id
     *
     * @param userId user's id
     */
    public ResponseEntity<Object> deleteUser(Long userId) {
        return delete(SLASH_PATH + userId);
    }

    /**
     * create GET-request to get all users
     *
     * @return GET-request
     */
    public ResponseEntity<Object> getAllUsers() {
        return get(EMPTY_PATH);
    }
}

