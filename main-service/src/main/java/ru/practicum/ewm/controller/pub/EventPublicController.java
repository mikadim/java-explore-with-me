package ru.practicum.ewm.controller.pub;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@AllArgsConstructor
@Controller
@RequestMapping("/events")
public class EventPublicController {
    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final EventService eventService;

    @GetMapping()
    public ResponseEntity<List<EventShortDto>> getEvents(@RequestParam(value = "text", required = false) String text,
                                                         @RequestParam(name = "categories", required = false) List<Integer> categories,
                                                         @RequestParam(name = "paid", required = false) Boolean paid,
                                                         @RequestParam(name = "rangeStart", required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeStart,
                                                         @RequestParam(name = "rangeEnd", required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeEnd,
                                                         @RequestParam(name = "onlyAvailable", defaultValue = "false") Boolean onlyAvailable,
                                                         @RequestParam(name = "sort", required = false) String sort,
                                                         @PositiveOrZero  @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                         @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                                         HttpServletRequest request) {
        log.info("Получение событий с позиции={}, размер={}", from, size);
        eventService.postRequestToStat(request);

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        Page<EventShortDto> events = eventService.getEventsByFiltersShortDto(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);
        return new ResponseEntity<>(events.getContent(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventFullDto> getEvent(@PathVariable(name = "id") Long id, HttpServletRequest request) {
        log.info("Получение события с id={}", id);
        eventService.postRequestToStat(request);
        return new ResponseEntity<>(eventService.getEventBuIdShortDto(id), HttpStatus.OK);
    }
}
