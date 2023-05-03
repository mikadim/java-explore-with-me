package ru.practicum.ewm.dto.user;

import lombok.Value;

@Value
public class UserRatingDto {
    Long id;
    String name;
    Long rate;
}
