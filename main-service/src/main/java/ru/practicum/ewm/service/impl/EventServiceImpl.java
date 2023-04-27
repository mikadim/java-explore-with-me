package ru.practicum.ewm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.dto.StatCountDto;
import ru.practicum.dto.StatDto;
import ru.practicum.ewm.common.StatRestClient;
import ru.practicum.ewm.dto.RequestStatusUpdateStatuses;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.event.eventupdate.UpdateEventAdminRequestDto;
import ru.practicum.ewm.dto.event.eventupdate.UpdateEventRequestDto;
import ru.practicum.ewm.dto.event.eventupdate.UpdateEventUserRequestDto;
import ru.practicum.ewm.exception.ConstraintException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.EventService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
public class EventServiceImpl implements EventService {
    private static final String EVENT_PATH = "/events/";
    private static final String DATE_TIME_FORMATTER = "yyyy-MM-dd HH:mm:ss";
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper mapper;
    private final StatRestClient restClient;
    private final ObjectMapper objectMapper;
    private final RequestMapper requestMapper;
    private final String applicationName;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository, RequestRepository requestRepository,
                            CategoryRepository categoryRepository, EventMapper mapper, StatRestClient restClient,
                            ObjectMapper objectMapper, RequestMapper requestMapper, @Value("${spring.application.name}") String appName) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.requestMapper = requestMapper;
        this.applicationName = appName;
    }

    @Override
    @Transactional
    public EventFullDto createEvent(NewEventDto dto, Long userId) {
        Event event = mapper.toEvent(dto);
        event.setInitiator(userRepository.getReferenceById(userId));
        event.setCategory(categoryRepository.getReferenceById(event.getCategory().getId()));
        Event savedEvent = eventRepository.save(event);
        return eventRepository.getEventsWithConfirmedRequestFullView(List.of(savedEvent.getId()), null).get(0);
    }

    @Override
    public List<EventShortDto> getEventsList(Set<Event> eventsForGenerationDto) {
        if (eventsForGenerationDto.size() > 0) {
            Set<Long> eventsId = eventsForGenerationDto.stream().map(Event::getId).collect(Collectors.toSet());
            List<Event> events = eventRepository.getEventsFromSet(eventsId);
            List<EventShortDto> eventShortDtos = mapper.toEventShortDtos(events);
            Map<Long, Long> eventViews = getEventViews(events);
            if (eventViews.size() > 0) {
                for (EventShortDto dto : eventShortDtos) {
                    if (eventViews.containsKey(dto.getId())) {
                        dto.setViews(eventViews.get(dto.getId()));
                    }
                }
            }
            return eventShortDtos;
        }
        return Collections.emptyList();
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        List<EventFullDto> eventFullDtos = eventRepository.getEventsWithConfirmedRequestFullView(List.of(eventId), userId);
        if (!eventFullDtos.isEmpty()) {
            return setViewsEventFullDto(eventFullDtos).get(0);
        } else {
            throw new ObjectNotFoundException(String.format("Event with id=%d was not found", eventId));
        }
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResultDto updateRequestsStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest dto) {
        List<EventFullDto> eventFullDtos = eventRepository.getEventsWithConfirmedRequestFullView(List.of(eventId), userId);
        if (eventFullDtos.isEmpty()) {
            throw new ObjectNotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        if (CollectionUtils.isEmpty(dto.getRequestIds()) || dto.getStatus() == null) {
            return new EventRequestStatusUpdateResultDto();
        }
        EventFullDto eventFullDto = eventFullDtos.get(0);
        if (dto.getStatus() == RequestStatusUpdateStatuses.CONFIRMED) {
            if (eventFullDto.getParticipantLimit() == 0 || !eventFullDto.getRequestModeration()) {
                return new EventRequestStatusUpdateResultDto();
            }
            if (eventFullDto.getParticipantLimit().equals(eventFullDto.getConfirmedRequests())) {
                throw new ConstraintException("The participant limit has been reached");
            }
        }
        List<Request> requests = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);
        List<Request> requestsForUpdate = requests.stream()
                .filter(request -> dto.getRequestIds().contains(request.getId())).collect(Collectors.toList());
        if (dto.getStatus() == RequestStatusUpdateStatuses.CONFIRMED &&
                (requestsForUpdate.size() + eventFullDto.getConfirmedRequests()) > eventFullDto.getParticipantLimit()) {
            throw new ConstraintException("The participant limit has been reached");
        }
        if (requestsForUpdate.size() != dto.getRequestIds().size()) {
            throw new ConstraintException("Request must have status PENDING");
        }
        for (Request request : requestsForUpdate) {
            request.setStatus(RequestStatusUpdateStatuses.getRequestStatus(dto.getStatus()));
        }
        requestRepository.saveAll(requestsForUpdate);
        EventRequestStatusUpdateResultDto result = new EventRequestStatusUpdateResultDto();
        if (dto.getStatus() == RequestStatusUpdateStatuses.CONFIRMED) {
            result.setConfirmedRequests(requestMapper.toParticipationRequestDtos(requestsForUpdate));
            if ((requestsForUpdate.size() + eventFullDto.getConfirmedRequests()) == eventFullDto.getParticipantLimit()) {
                List<Request> requestsForReject = requests.stream()
                        .filter(request -> !dto.getRequestIds().contains(request.getId())).collect(Collectors.toList());
                for (Request request : requestsForReject) {
                    request.setStatus(RequestStatus.REJECTED);
                }
                requestRepository.saveAll(requestsForReject);
                result.setRejectedRequests(requestMapper.toParticipationRequestDtos(requestsForReject));
            }
        }
        if (dto.getStatus() == RequestStatusUpdateStatuses.REJECTED) {
            result.setRejectedRequests(requestMapper.toParticipationRequestDtos(requestsForUpdate));
        }
        return result;
    }

    @Override
    @Transactional
    public <T extends UpdateEventRequestDto> EventFullDto updateEvent(Long userId, Long eventId, T dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Event with id=%d was not found", eventId)));

        if (dto.getEventDate() != null && dto.getEventDate().minusHours(2).isBefore(LocalDateTime.now())) {
            throw new ConstraintException("Field: eventDate. Error: должно содержать дату, которая еще ненаступила. " +
                    "Value: " + dto.getEventDate());
        }
        if (dto instanceof UpdateEventUserRequestDto) {
            checkEventParametersWhenUserUpdateIt(event, (UpdateEventUserRequestDto) dto, userId);
        }
        if (dto instanceof UpdateEventAdminRequestDto) {
            checkEventParametersWhenAdminUpdateIt(event, (UpdateEventAdminRequestDto) dto);
        }
        if (!StringUtils.isBlank(dto.getAnnotation())) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            event.setCategory(categoryRepository.getReferenceById(dto.getCategory()));
        }
        if (!StringUtils.isBlank(dto.getDescription())) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getLocation() != null) {
            if (dto.getLocation().getLat() != null) {
                event.setLat(dto.getLocation().getLat());
            }
            if (dto.getLocation().getLon() != null) {
                event.setLon(dto.getLocation().getLon());
            }
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto instanceof UpdateEventUserRequestDto) {
            if (((UpdateEventUserRequestDto) dto).getStateAction() != null) {
                UpdateEventUserRequestDto.StateActionStatus stateActionStatus = ((UpdateEventUserRequestDto) dto).getStateAction();
                UpdateEventUserRequestDto.StateActionStatus.getEventStatus(stateActionStatus)
                        .ifPresent(status -> {
                            event.setState(status);
                        });
            }
        }
        if (dto instanceof UpdateEventAdminRequestDto) {
            if (((UpdateEventAdminRequestDto) dto).getStateAction() != null) {
                UpdateEventAdminRequestDto.StateActionStatus stateActionStatus = ((UpdateEventAdminRequestDto) dto).getStateAction();
                UpdateEventAdminRequestDto.StateActionStatus.getEventStatus(stateActionStatus)
                        .ifPresent(status -> {
                            event.setState(status);
                            if (status == EventStatus.PUBLISHED) {
                                event.setPublishedOn(LocalDateTime.now());
                            }
                        });
            }
        }
        if (!StringUtils.isBlank(dto.getTitle())) {
            event.setTitle(dto.getTitle());
        }
        return getUserEventById(userId, eventId);
    }

    @Override
    public Page<EventFullDto> getAllUserEvents(Long userId, Integer from, Integer size) {
        return getEventsByFilters(List.of(userId), null, null, null, null, from, size, null);
    }

    @Override
    public Page<EventFullDto> getEventsByFilters(List<Long> userIds, List<EventStatus> eventStatus, List<Integer> categories,
                                                 LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size, Long eventId) {
        Sort sortByEventDate = Sort.by(Sort.Direction.DESC, "eventDate");
        Pageable page = PageRequest.of(from / size, size, sortByEventDate);
        Page<EventFullDto> eventDtosPage = eventRepository.getAllUserEvents(userIds, eventStatus, categories, rangeStart,
                rangeEnd, eventId, page);
        List<EventFullDto> eventDtos = eventDtosPage.getContent();
        return new PageImpl<>(setViewsEventFullDto(eventDtos), eventDtosPage.getPageable(), eventDtosPage.getTotalElements());
    }

    @Override
    public Page<EventShortDto> getEventsByFiltersShortDto(String text, List<Integer> categories, Boolean paid,
                                                          LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                          Boolean onlyAvailable, String sort, Integer from, Integer size) {
        if (!StringUtils.isBlank(text)) {
            text = text.trim().toLowerCase();
        }
        Sort sortByEventDate = Sort.by(Sort.Direction.DESC, "id");
        Pageable page = PageRequest.of(from / size, size, sortByEventDate);
        Page<Event> eventPage = eventRepository.getEventsWithPublicFilters(text, categories, paid, rangeStart,
                rangeEnd, onlyAvailable, page);
        List<Event> events = eventPage.getContent();
        List<EventShortDto> eventShortDtos = mapper.toEventShortDtos(events);
        Map<Long, Long> eventViews = getEventViews(events);
        if (eventViews.size() > 0) {
            for (EventShortDto dto : eventShortDtos) {
                if (eventViews.containsKey(dto.getId())) {
                    dto.setViews(eventViews.get(dto.getId()));
                }
            }
        }

        Comparator<EventShortDto> comparing = Comparator.comparing(EventShortDto::getId);
        if (!StringUtils.isBlank(sort))
            switch (sort.trim().toLowerCase()) {
                case "event_date":
                    comparing = Comparator.comparing(EventShortDto::getEventDate);
                    break;
                case "views":
                    comparing = Comparator.comparing(EventShortDto::getViews);
                    break;
            }
        return new PageImpl<>(eventShortDtos.stream().sorted(comparing).collect(Collectors.toList()),
                eventPage.getPageable(), eventPage.getTotalElements());

    }

    @Override
    public EventFullDto getEventBuIdShortDto(Long id) {
        Page<EventFullDto> eventsPage = getEventsByFilters(null, List.of(EventStatus.PUBLISHED), null,
                null, null, 0, 1, id);
        if (eventsPage.getContent().isEmpty()) {
            throw new ObjectNotFoundException(String.format("Event with id=%d was not found", id));
        }
        return eventsPage.getContent().get(0);
    }

    @Override
    public void postRequestToStat(HttpServletRequest request) {
        StatDto statDto = new StatDto(applicationName, request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER)));
        restClient.post(statDto);
    }

    private List<EventFullDto> setViewsEventFullDto(List<EventFullDto> eventFullDto) {
        Set<Long> eventsId = eventFullDto.stream().map(EventFullDto::getId).collect(Collectors.toSet());
        LocalDateTime startCountEventViews = eventFullDto.stream()
                .filter(dto -> dto.getPublishedOn() != null)
                .map(EventFullDto::getPublishedOn)
                .min(LocalDateTime::compareTo).or(() -> Optional.of(LocalDateTime.now())).get();
        Map<Long, Long> stats = getStats(startCountEventViews, LocalDateTime.now(), true, eventsId);
        if (stats.size() > 0) {
            for (EventFullDto dto : eventFullDto) {
                if (stats.containsKey(dto.getId())) {
                    dto.setViews(stats.get(dto.getId()));
                }
            }
        }
        return eventFullDto;
    }

    public Map<Long, Long> getStats(LocalDateTime start, LocalDateTime end, Boolean unique, Set<Long> uris) {

        StringBuilder builder = new StringBuilder();
        for (Long uri : uris) {
            builder.append(EVENT_PATH + uri + ",");
        }
        ResponseEntity<Object[]> response = restClient.get(start, end, unique, builder.toString());
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Object[] object = response.getBody();
            List<StatCountDto> collect = Arrays.stream(object).map(o -> objectMapper.convertValue(o, StatCountDto.class))
                    .filter(dto -> dto.getApp().equals(applicationName))
                    .collect(Collectors.toList());
            Map<Long, Long> resultMap = new HashMap<>();
            for (StatCountDto dto : collect) {
                Long id = NumberUtils.toLong(StringUtils.substringAfterLast(dto.getUri(), "/"));
                if (id > 0) {
                    resultMap.put(id, dto.getHits());
                }
            }
            return resultMap;
        } else {
            return Collections.emptyMap();
        }
    }

    private void checkEventParametersWhenUserUpdateIt(Event event, UpdateEventUserRequestDto dto, Long userId) {
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ObjectNotFoundException(String.format("Event with id=%d was not available", event.getId()));
        }
        if (event.getState() == EventStatus.PUBLISHED) {
            throw new ConstraintException("Only pending or canceled events can be changed");
        }
    }

    private void checkEventParametersWhenAdminUpdateIt(Event event, UpdateEventAdminRequestDto dto) {
        if (dto.getStateAction() != null) {
            if (dto.getStateAction() == UpdateEventAdminRequestDto.StateActionStatus.PUBLISH_EVENT &&
                    event.getState() != EventStatus.PENDING) {
                throw new ConstraintException("Cannot publish the event because it's not in the right state: PUBLISHED");
            }
            if (dto.getStateAction() == UpdateEventAdminRequestDto.StateActionStatus.REJECT_EVENT &&
                    event.getState() == EventStatus.PUBLISHED) {
                throw new ConstraintException("Cannot reject the event because it's not in the right state: PUBLISHED");
            }
        }
    }

    private Map<Long, Long> getEventViews(List<Event> events) {
        Set<Long> eventsId = events.stream().map(Event::getId).collect(Collectors.toSet());
        LocalDateTime startCountEventViews = events.stream()
                .filter(event -> event.getPublishedOn() != null)
                .map(Event::getPublishedOn)
                .min(LocalDateTime::compareTo).or(() -> Optional.of(LocalDateTime.now())).get();
        return getStats(startCountEventViews, LocalDateTime.now(), true, eventsId);
    }
}
