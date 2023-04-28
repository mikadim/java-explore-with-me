package ru.practicum.ewm.controller.priv;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.event.eventupdate.UpdateEventUserRequestDto;
import ru.practicum.ewm.service.EventService;
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
        return ResponseEntity.ok(eventService.getUserEventById(userId, eventId));
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResultDto> updateRequestsStatuses(@RequestBody @Valid EventRequestStatusUpdateRequest dto,
                                                                                    @PathVariable(name = "userId") Long userId,
                                                                                    @PathVariable(name = "eventId") Long eventId) {
        log.info("Обновления пользователем id={}, в событии id={}, статуса заявок: {}", userId, eventId, dto);
        return ResponseEntity.ok(eventService.updateRequestsStatuses(userId, eventId, dto));
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getEventRequests(@PathVariable(name = "userId") Long userId,
                                                                          @PathVariable(name = "eventId") Long eventId) {
        log.info("Получение пользователем id={} запросов на участие в событии id={}", userId, eventId);
        return ResponseEntity.ok(requestService.getUserEventRequests(userId, eventId));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@RequestBody @Valid UpdateEventUserRequestDto dto,
                                                    @PathVariable(name = "userId") Long userId,
                                                    @PathVariable(name = "eventId") Long eventId) {
        log.info("Обновления пользователем id={}, события id={}", userId, eventId);
        return ResponseEntity.ok(eventService.updateEvent(userId, eventId, dto));
    }

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getUserEvents(@PathVariable(name = "userId") Long userId,
                                                            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получение событий пользователя id={}", userId);
        return ResponseEntity.ok(eventService.getAllUserEvents(userId, from, size));
    }
}
