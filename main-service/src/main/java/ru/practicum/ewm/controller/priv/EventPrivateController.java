package ru.practicum.ewm.controller.priv;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.event.eventupdate.UpdateEventUserRequestDto;
import ru.practicum.ewm.model.ReactionOnEvent;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.service.ReactionService;
import ru.practicum.ewm.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@AllArgsConstructor
@Controller
@RequestMapping("/users/{userId}/events")
public class EventPrivateController {
    private final EventService eventService;
    private final RequestService requestService;
    private final ReactionService reactionService;

    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(@RequestBody @Valid NewEventDto dto,
                                                    @PathVariable(name = "userId") Long userId) {
        log.info("Создание пользователем id={}, нового события: {}", userId, dto);
        return new ResponseEntity<>(eventService.createEvent(dto, userId), HttpStatus.CREATED);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getUserEventById(@PathVariable(name = "userId") Long userId,
                                                         @PathVariable(name = "eventId") Long eventId) {
        log.info("Получение пользователем id={}, события id={}", userId, eventId);
        return new ResponseEntity<>(eventService.getUserEventById(userId, eventId), HttpStatus.OK);
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResultDto> updateRequestsStatuses(@RequestBody @Valid EventRequestStatusUpdateRequest dto,
                                                                                    @PathVariable(name = "userId") Long userId,
                                                                                    @PathVariable(name = "eventId") Long eventId) {
        log.info("Обновления пользователем id={}, в событии id={}, статуса заявок: {}", userId, eventId, dto);
        return new ResponseEntity<>(eventService.updateRequestsStatuses(userId, eventId, dto), HttpStatus.OK);
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getEventRequests(@PathVariable(name = "userId") Long userId,
                                                                          @PathVariable(name = "eventId") Long eventId) {
        log.info("Получение пользователем id={} запросов на участие в событии id={}", userId, eventId);
        return new ResponseEntity<>(requestService.getUserEventRequests(userId, eventId), HttpStatus.OK);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@RequestBody @Valid UpdateEventUserRequestDto dto,
                                                    @PathVariable(name = "userId") Long userId,
                                                    @PathVariable(name = "eventId") Long eventId) {
        log.info("Обновления пользователем id={}, события id={}", userId, eventId);
        return new ResponseEntity<>(eventService.updateEvent(userId, eventId, dto), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getUserEvents(@PathVariable(name = "userId") Long userId,
                                                            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получение событий пользователя id={}", userId);
        Page<EventFullDto> allUserEvents = eventService.getAllUserEvents(userId, from, size);
        return new ResponseEntity<>(allUserEvents.getContent(), HttpStatus.OK);
    }

    @PostMapping("/{eventId}/reaction")
    @ResponseStatus(HttpStatus.CREATED)
    public void createReactionOnEvent(@PathVariable(name = "userId") Long userId,
                                      @PathVariable(name = "eventId") Long eventId,
                                      @RequestParam(name = "react") ReactionOnEvent.ReactionStatus react) {
        log.info("Добавление пользователем id={} на событие  id={}, реакции: {}", userId, eventId, react);
        reactionService.createOrUpdateReaction(userId, eventId, react);
    }
}
