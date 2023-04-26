package ru.practicum.ewm.controller.priv;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.RequestService;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/users/{userId}/requests")
public class RequestPrivateController {
    private final RequestService service;

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> createRequest(@PathVariable(name = "userId") Long userId,
                                                                 @RequestParam(name = "eventId") Long eventId) {
        log.info("Добавление запроса на участие в событии id={}, пользователь id={}", eventId, userId);
        return new ResponseEntity(service.createRequest(userId, eventId), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getRequests(@PathVariable(name = "userId") Long userId) {
        log.info("Получение всех запросов пользователя id={}", userId);
        return new ResponseEntity(service.getRequests(userId), HttpStatus.OK);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(@PathVariable(name = "userId") Long userId,
                                                                 @PathVariable(name = "requestId") Long requestId) {
        log.info("Отмена запроса id={}, пользователем id={}", requestId, userId);
        return new ResponseEntity(service.cancelRequest(userId, requestId), HttpStatus.OK);
    }
}
