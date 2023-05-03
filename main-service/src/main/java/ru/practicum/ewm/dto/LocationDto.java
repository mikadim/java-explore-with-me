package ru.practicum.ewm.dto;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class LocationDto {
    @NotNull
    Float lat;
    @NotNull
    Float lon;
}
