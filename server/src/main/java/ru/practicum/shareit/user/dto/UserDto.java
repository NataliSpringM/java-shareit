package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * User DTO model.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class UserDto {
    Long id;
    String name;
    String email;

}

