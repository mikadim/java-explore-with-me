package ru.practicum.ewm.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.ReactionOnEventDto;
import ru.practicum.ewm.exception.ConstraintException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.mapper.RatingMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventStatus;
import ru.practicum.ewm.model.ReactionOnEvent;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RatingRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.service.RatingService;

import java.time.LocalDateTime;
import java.util.List;

@Transactional(readOnly = true)
@AllArgsConstructor
@Service
public class RatingServiceImpl implements RatingService {
    private final RequestRepository requestRepository;
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ReactionOnEventDto createReaction(Long userId, Long eventId, ReactionOnEvent.ReactionStatus reactionStatus) {
        Request request = requestRepository.findByEventIdAndRequesterIdAndEventState(eventId, userId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new ConstraintException("Событие id=" + eventId + " недоступно, либо вы не подавали " +
                        "заявку на участие в нем"));
        ReactionOnEvent reaction = new ReactionOnEvent();
        reaction.setParticipant(request.getRequester());
        reaction.setEvent(request.getEvent());
        reaction.setReaction(reactionStatus);
        reaction.setTimestamp(LocalDateTime.now());
        ratingRepository.save(reaction);
        return ratingMapper.toReactionOnEventDto(reaction);
    }

    @Override
    @Transactional
    public ReactionOnEventDto updateReaction(Long userId, Long eventId, ReactionOnEvent.ReactionStatus reactionStatus) {
        ReactionOnEvent reactionOnEvent = ratingRepository.findByParticipantIdAndEventId(userId, eventId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Пользователь id=%d еще не оставлял реакцию на событие id=%d", userId, eventId)));
        if (reactionOnEvent.getReaction() != reactionStatus) {
            reactionOnEvent.setReaction(reactionStatus);
        } else {
            throw new ConstraintException(String.format("Для собыитя id=%d уже отмечена реакция: %s", eventId, reactionStatus));
        }
        return ratingMapper.toReactionOnEventDto(reactionOnEvent);
    }

    @Override
    @Transactional
    public void deleteReaction(Long userId, Long eventId) {
        ReactionOnEvent reactionOnEvent = ratingRepository.findByParticipantIdAndEventId(userId, eventId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Пользователь id=%d еще не оставлял реакцию на событие id=%d", userId, eventId)));
        ratingRepository.deleteById(reactionOnEvent.getId());
    }

    @Override
    public List<ReactionOnEventDto> getReactionsOnEvent(Long eventId, Integer from, Integer size) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Event with id=%d was not found", eventId)));
        Sort sortByTimestamp = Sort.by(Sort.Direction.DESC, "id");
        Pageable page = PageRequest.of(from / size, size, sortByTimestamp);
        Page<ReactionOnEvent> reactions;
        reactions = ratingRepository.findByEventId(event.getId(), page);
        return ratingMapper.toReactionOnEventDtos(reactions.getContent());
    }
}
