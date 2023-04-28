package ru.practicum.ewm.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.ReactionOnEvent;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ReactionRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.service.ReactionService;
import ru.practicum.ewm.service.RequestService;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@AllArgsConstructor
@Service
public class ReactionServiceImpl implements ReactionService {
    private final RequestService requestService;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final ReactionRepository reactionRepository;

    @Override
    @Transactional
    public void createOrUpdateReaction(Long userId, Long eventId, ReactionOnEvent.ReactionStatus react) {
        Optional<ReactionOnEvent> reactionOnEvent = reactionRepository.findByParticipantIdAndEventId(userId, eventId);
        if (reactionOnEvent.isPresent()) {
            reactionOnEvent.get().setReaction(react);
            return;
        }


        Event event = eventRepository.getReferenceById(eventId);


        try {

            List<Request> userEventRequests = requestRepository.getUserEventRequests(userId, event.getId());
        } catch (EntityNotFoundException e) {
            throw new ObjectNotFoundException("Event with id=" + userId + " was not found", e.getCause());
        }

    }
}
