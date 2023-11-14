package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.util.constants.Constants.*;

/**
 * UserController tests
 */

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserClient userClient;

    Long userId;


    @BeforeEach
    public void before() {
        userId = 1L;
    }

    /**
     * test addUser method
     * POST-request "/users"
     * when user's data are valid
     * should return status ok
     * should invoke service addUser method and return result
     */

    @Test
    @SneakyThrows
    public void addUser_whenUserIsValid_statusIsOk_andInvokeService() {

        //addUser UserDto with valid fields
        UserDto user = UserDto.builder()
                .id(userId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //map Dto to String
        String userString = objectMapper.writeValueAsString(user);

        //perform request and check status
        mockMvc.perform(post(USERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userString))
                .andExpect(status().isOk());

        //verify invokes
        verify(userClient).addUser(any(UserDto.class));


    }

    /**
     * test addUser method
     * POST-request "/users"
     * when user's data are not valid: name is empty
     * should return status bad request
     * should not invoke service addUser method
     */

    @Test
    @SneakyThrows
    public void addUser_whenUserHasEmptyName_statusIsBadRequest_doesNotInvokeService() {

        //create invalid field
        String emptyString = "";

        //create UserDto with invalid field
        UserDto invalidUser = UserDto.builder()
                .id(userId)
                .name(emptyString)
                .email("Olga@yandex.ru").build();

        //map Dto to String
        String invalidUserString = objectMapper.writeValueAsString(invalidUser);

        //perform tested request and check status
        mockMvc.perform(post(USERS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserString))
                .andExpect(status().isBadRequest());

        //verify invokes
        verify(userClient, Mockito.never()).addUser(any());
    }

    /**
     * test addUser method
     * POST-request "/users"
     * when user's data are not valid: email is empty
     * should return status bad request
     * should not invoke service addUser method
     */

    @Test
    @SneakyThrows
    public void addUser_whenUserHasEmptyEmail_statusIsBadRequest_doesNotInvokeService() {

        //create invalid field
        String emptyString = "";

        //create UserDto with invalid field
        UserDto invalidUser = UserDto.builder()
                .id(userId)
                .name("Olga")
                .email(emptyString)
                .build();

        //map Dto to String
        String invalidUserString = objectMapper.writeValueAsString(invalidUser);

        //perform tested request and check status
        mockMvc.perform(post(USERS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserString))
                .andExpect(status().isBadRequest());

        //verify invokes
        verify(userClient, Mockito.never()).addUser(any());
    }

    /**
     * test addUser method
     * POST-request "/users"
     * when user's data are not valid: name is null
     * should return status bad request
     * should not invoke service addUser method
     */

    @Test
    @SneakyThrows
    public void addUser_whenUserHasNullName_statusIsBadRequest_doesNotInvokeService() {

        //create UserDto with null field
        UserDto invalidUser = UserDto.builder()
                .id(userId)
                .name(null)
                .email("Olga@yandex.ru")
                .build();
        //map Dto to String
        String invalidUserString = objectMapper.writeValueAsString(invalidUser);

        //perform tested request and check status
        mockMvc.perform(post(USERS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserString))
                .andExpect(status().isBadRequest());

        //verify invokes
        verify(userClient, Mockito.never()).addUser(any());

    }

    /**
     * test addUser method
     * POST-request "/users"
     * when user's data are not valid: email is null
     * should return status bad request
     * should not invoke service addUser method
     */

    @Test
    @SneakyThrows
    public void addUser_whenUserHasNullEmail_statusIsBadRequest_doesNotInvokeService() {

        //create UserDto with null field
        UserDto invalidUser = UserDto.builder()
                .id(userId)
                .name("Olga")
                .email(null)
                .build();

        //map Dto to String
        String invalidUserString = objectMapper.writeValueAsString(invalidUser);

        //perform tested request and check status
        mockMvc.perform(post(USERS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserString))
                .andExpect(status().isBadRequest());

        //verify invokes
        verify(userClient, Mockito.never()).addUser(any());

    }

    /**
     * test addUser method
     * POST-request "/users"
     * when user's data are not valid: email is invalid
     * should return status bad request
     * should not invoke service addUser method
     */

    @Test
    @SneakyThrows
    public void addUser_whenUserHasInvalidEmail_statusIsBadRequest_doesNotInvokeService() {

        //create invalid field
        String invalidEmail = "@ku";

        //create UserDto with invalid field
        UserDto invalidUser = UserDto.builder()
                .id(userId)
                .name("Olga")
                .email(invalidEmail)
                .build();

        //map Dto to String
        String invalidUserString = objectMapper.writeValueAsString(invalidUser);

        //perform tested request and check status
        mockMvc.perform(post(USERS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserString))
                .andExpect(status().isBadRequest());

        //verify invokes
        verify(userClient, Mockito.never()).addUser(any());
    }

    /**
     * test getUserById method
     * GET-request "/users/{userId}"
     * should return status ok
     * should invoke service getUserById method and return result
     */

    @SneakyThrows
    @Test
    public void getUserById_statusIsOk_andInvokeService() {

        //perform request and check status
        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk());

        //verify invokes
        verify(userClient).getUserById(userId);

    }

    /**
     * test updateUser method
     * PATCH-request "/users/{userId}"
     * when user's data is valid
     * should return status ok
     * should invoke service updateUser method and return result
     */

    @Test
    @SneakyThrows
    public void updateUser_whenUserIsValid_statusIsOk_andInvokeService() {

        //create UserDto with valid fields
        UserDto validUser = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //map Dto to String
        String validUserString = objectMapper.writeValueAsString(validUser);

        //perform request and check status
        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUserString))
                .andExpect(status().isOk());

        //verify invokes
        verify(userClient).updateUser(any(UserDto.class), eq(userId));

    }

    /**
     * test updateUser method
     * PATCH-request "/users/{userId}"
     * when user's data is invalid: email is invalid
     * should return status bad request
     * should not invoke service updateUser method
     */

    @Test
    @SneakyThrows
    public void updateUser_whenUserHasInvalidEmail_statusIsBadRequest_doesNotInvokeService() {

        //create invalid field
        String invalidEmail = "@ku";

        //create UserDto with invalid field
        UserDto invalidUser = UserDto.builder()
                .id(userId)
                .name("Olga")
                .email(invalidEmail)
                .build();

        //map Dto to String
        String invalidUserString = objectMapper.writeValueAsString(invalidUser);

        //perform tested request and check status
        mockMvc.perform(patch(USERS_PATH + USER_ID_PATH_VARIABLE, userId)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserString))
                .andExpect(status().isBadRequest());

        //verify invokes
        verify(userClient, Mockito.never()).updateUser(any(), anyLong());
    }

    /**
     * test deleteUserById method
     * DELETE-request "/users/{userId}"
     * should return status ok
     * should invoke service deleteUserById method
     */

    @Test
    @SneakyThrows
    public void delete_statusIsOk_invokeService() {

        //perform request and check status
        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        //verify invokes
        verify(userClient).deleteUser(userId);
    }

    /**
     * test getAllUsers method
     * GET-request "/users"
     * should return status ok
     * should invoke service getAllUsers method and return result
     */

    @Test
    @SneakyThrows
    public void getList_statusIsOk_invokeService() {


        //perform request and check status
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        //verify invokes
        verify(userClient).getAllUsers();

    }
}
