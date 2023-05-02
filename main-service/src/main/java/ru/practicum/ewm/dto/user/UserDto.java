package ru.practicum.ewm.dto.user;

import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
public class UserDto {
    Long id;
    @NotBlank
    @Size(max = 50)
    String name;
    @NotBlank
    @Email
    @Size(max = 50)
    String email;
}
