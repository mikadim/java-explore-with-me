package ru.practicum.ewm.dto;

import lombok.Value;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.model.ReactionOnEvent;

@Value
public class ReactionOnEventDto {
    Long eventId;
    UserShortDto participant;
    ReactionOnEvent.ReactionStatus reaction;
}
