package ru.practicum.ewm.controller.priv;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.dto.ReactionOnEventDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.event.eventupdate.UpdateEventUserRequestDto;
import ru.practicum.ewm.dto.user.UserRatingDto;
import ru.practicum.ewm.model.ReactionOnEvent;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.service.RatingService;
import ru.practicum.ewm.service.RequestService;
import ru.practicum.ewm.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@AllArgsConstructor
@Controller
@RequestMapping("/users/{userId}")
public class EventPrivateController {
    private static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    private final EventService eventService;
    private final RequestService requestService;
    private final RatingService ratingService;
    private final UserService userService;

    @PostMapping("/events")
    public ResponseEntity<EventFullDto> createEvent(@RequestBody @Valid NewEventDto dto,
                                                    @PathVariable(name = "userId") Long userId) {
        log.info("Создание пользователем id={}, нового события: {}", userId, dto);
        return new ResponseEntity<>(eventService.createEvent(dto, userId), HttpStatus.CREATED);
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventWithReactionFullDto> getUserEventById(@PathVariable(name = "userId") Long userId,
                                                         @PathVariable(name = "eventId") Long eventId) {
        log.info("Получение пользователем id={}, события id={}", userId, eventId);
        return ResponseEntity.ok(eventService.getUserEventById(userId, eventId));
    }

    @PatchMapping("/events/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResultDto> updateRequestsStatuses(@RequestBody @Valid EventRequestStatusUpdateRequest dto,
                                                                                    @PathVariable(name = "userId") Long userId,
                                                                                    @PathVariable(name = "eventId") Long eventId) {
        log.info("Обновления пользователем id={}, в событии id={}, статуса заявок: {}", userId, eventId, dto);
        return ResponseEntity.ok(eventService.updateRequestsStatuses(userId, eventId, dto));
    }

    @GetMapping("/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getEventRequests(@PathVariable(name = "userId") Long userId,
                                                                          @PathVariable(name = "eventId") Long eventId) {
        log.info("Получение пользователем id={} запросов на участие в событии id={}", userId, eventId);
        return ResponseEntity.ok(requestService.getUserEventRequests(userId, eventId));
    }

    @PatchMapping("/events/{eventId}")
    public ResponseEntity<EventWithReactionFullDto> updateEvent(@RequestBody @Valid UpdateEventUserRequestDto dto,
                                                    @PathVariable(name = "userId") Long userId,
                                                    @PathVariable(name = "eventId") Long eventId) {
        log.info("Обновления пользователем id={}, события id={}", userId, eventId);
        return ResponseEntity.ok(eventService.updateEvent(userId, eventId, dto));
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventFullDto>> getUserEvents(@PathVariable(name = "userId") Long userId,
                                                            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                                            @RequestParam(name = "sort", defaultValue = "EVENT_DATE") EventSortingTypes sort) {
        log.info("Получение событий пользователя id={}", userId);
        return ResponseEntity.ok(eventService.getAllUserEvents(userId, from, size, sort));
    }

    @PostMapping("/events/{eventId}/reaction")
    public ResponseEntity<ReactionOnEventDto> createReactionOnEvent(@PathVariable(name = "userId") Long userId,
                                      @PathVariable(name = "eventId") Long eventId,
                                      @RequestParam(name = "react") ReactionOnEvent.ReactionStatus reactionStatus) {
        log.info("Добавление пользователем id={} на событие  id={}, реакции: {}", userId, eventId, reactionStatus);
        return new ResponseEntity<>(ratingService.createReaction(userId, eventId, reactionStatus), HttpStatus.CREATED);
    }

    @PatchMapping("/events/{eventId}/reaction")
    public ResponseEntity<ReactionOnEventDto> updateReactionOnEvent(@PathVariable(name = "userId") Long userId,
                                      @PathVariable(name = "eventId") Long eventId,
                                      @RequestParam(name = "react") ReactionOnEvent.ReactionStatus reactionStatus) {
        log.info("Добавление пользователем id={} на событие id={}, реакции: {}", userId, eventId, reactionStatus);
        return ResponseEntity.ok(ratingService.updateReaction(userId, eventId, reactionStatus));
    }

    @DeleteMapping("/events/{eventId}/reaction")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateReactionOnEvent(@PathVariable(name = "userId") Long userId,
                                      @PathVariable(name = "eventId") Long eventId) {
        log.info("Отменить реакцию пользователя id={} на событие id={}", userId, eventId);
        ratingService.deleteReaction(userId, eventId);
    }

    @GetMapping("/events/{eventId}/reaction")
    public ResponseEntity<List<ReactionOnEventDto>> getReactionsOnEvent(@PathVariable(name = "eventId") Long eventId,
                                                                       @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                                       @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получить реакции пользователей на событие id={}", eventId);
        return ResponseEntity.ok(ratingService.getReactionsOnEvent(eventId, from, size));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<UserRatingDto>> getMostRatingUsersSinceEventPublishDate(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                                               @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                                                               @RequestParam(name = "eventDate", required = false) @DateTimeFormat(pattern = DATE_TIME) LocalDateTime eventPublishedDate) {
        log.info("Получить самых рейтинговых инициаторов событий");
        return ResponseEntity.ok(userService.getMostRatingUser(from, size, eventPublishedDate));
    }
}
