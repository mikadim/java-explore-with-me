package ru.practicum.ewm.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventWithReactionFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.repository.EventRepository;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {
    @Mapping(target = "category", source = "category.id")
    NewEventDto toNewEventDto(Event event);

    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(source = "location.lat", target = "lat")
    @Mapping(source = "location.lon", target = "lon")
    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "state", constant = "PENDING")
    Event toEvent(NewEventDto event);

    EventShortDto toEventShortDto(Event event);

    List<EventShortDto> toEventShortDtos(List<Event> event);

    @Mapping(source = "lat", target = "location.lat")
    @Mapping(source = "lon", target = "location.lon")
    EventFullDto toEventFullDtoWithoutRating(Event event);

    @Mapping(source = "event.lat", target = "location.lat")
    @Mapping(source = "event.lon", target = "location.lon")
    @Mapping(source = "event.annotation", target = "annotation")
    @Mapping(source = "event.category", target = "category")
    @Mapping(source = "event.confirmedRequests", target = "confirmedRequests")
    @Mapping(source = "event.createdOn", target = "createdOn")
    @Mapping(source = "event.description", target = "description")
    @Mapping(source = "event.eventDate", target = "eventDate")
    @Mapping(source = "event.id", target = "id")
    @Mapping(source = "event.initiator", target = "initiator")
    @Mapping(source = "event.paid", target = "paid")
    @Mapping(source = "event.participantLimit", target = "participantLimit")
    @Mapping(source = "event.publishedOn", target = "publishedOn")
    @Mapping(source = "event.requestModeration", target = "requestModeration")
    @Mapping(source = "event.state", target = "state")
    @Mapping(source = "event.title", target = "title")
    @Mapping(source = "rating", target = "rating")
    EventFullDto toEventFullDto(EventRepository.EventWithRating event);


    @IterableMapping(elementTargetType = EventFullDto.class)
    List<EventFullDto> toEventFullDtos(List<EventRepository.EventWithRating> event);

    @Mapping(source = "event.lat", target = "location.lat")
    @Mapping(source = "event.lon", target = "location.lon")
    @Mapping(source = "rating", target = "rating")
    EventWithReactionFullDto toEventWithReactionFullDto(Event event, Long rating);
}