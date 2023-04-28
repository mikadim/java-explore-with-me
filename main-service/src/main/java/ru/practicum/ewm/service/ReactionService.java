package ru.practicum.ewm.service;

import ru.practicum.ewm.model.ReactionOnEvent;

public interface ReactionService {

    void createOrUpdateReaction(Long userId, Long eventId, ReactionOnEvent.ReactionStatus  react);
}
