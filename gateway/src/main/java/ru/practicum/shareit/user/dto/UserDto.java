package ru.practicum.shareit.user.dto;

import lombok.*;
import ru.practicum.shareit.util.groups.Create;
import ru.practicum.shareit.util.groups.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * User DTO model.
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserDto {
    private Long id;
    @NotNull(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    private String name;
    @NotNull(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    @Email(groups = {Create.class, Update.class})
    private String email;

}
