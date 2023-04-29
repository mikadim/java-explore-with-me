package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.ReactionOnEvent;

import java.util.Optional;

public interface ReactionRepository extends JpaRepository<ReactionOnEvent, Long> {
    Optional<ReactionOnEvent> findByParticipantIdAndEventId(Long userId, Long eventId);

}
