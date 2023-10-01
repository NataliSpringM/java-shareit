package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Sprint add-controllers.
 * User model.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
@Validated
public class User {
    Long id;
    @NotNull
    @NotBlank
    String name;
    @NotNull
    @NotBlank
    @Email
    String email;
}
