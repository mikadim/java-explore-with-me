package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.event.eventupdate.UpdateEventRequestDto;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventStatus;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface EventService {
    EventFullDto createEvent(NewEventDto dto, Long serId);

    List<EventShortDto> getEventsList(Set<Event> events);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventRequestStatusUpdateResultDto updateRequestsStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest dto);

    <T extends UpdateEventRequestDto> EventFullDto updateEvent(Long userId, Long eventId, T dto);

    List<EventFullDto> getAllUserEvents(Long userId, Integer from, Integer size);

    List<EventFullDto> getEventsForPrivateUsersWithFilters(List<Long> userIds, List<EventStatus> eventStatus, List<Integer> categories,
                                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size,
                                                           Long eventId);

    List<EventShortDto> getEventsForPublicUsersWithFilters(String text, List<Integer> categories, Boolean paid,
                                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                                           EventSortingTypes sort, Integer from, Integer size);

    EventFullDto getPublishedEventById(Long id);

    void addStatisticsToStatServer(HttpServletRequest request);
}
