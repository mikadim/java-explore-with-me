package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.ReactionOnEventDto;
import ru.practicum.ewm.model.ReactionOnEvent;

import java.util.List;

@Mapper
public interface RatingMapper {
    @Mapping(target = "eventId", source = "event.id")
    ReactionOnEventDto toReactionOnEventDto(ReactionOnEvent reaction);
    List<ReactionOnEventDto> toReactionOnEventDtos(List<ReactionOnEvent> reaction);
}
