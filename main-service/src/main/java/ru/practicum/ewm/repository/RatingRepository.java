package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.ReactionOnEvent;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<ReactionOnEvent, Long> {
    Optional<ReactionOnEvent> findByParticipantIdAndEventId(Long userId, Long eventId);

    List<ReactionOnEvent> findFirst3ByEventIdOrderByTimestampDesc(Long eventId);

    Page<ReactionOnEvent> findByEventId(Long eventId, Pageable page);

    @Query("select count(b.id) - count(c.id) from ReactionOnEvent a " +
            "left join ReactionOnEvent b on b.event.id = a.event.id and b.reaction = 'LIKE' " +
            "left join ReactionOnEvent c on c.event.id = a.event.id and b.reaction = 'DISLIKE' " +
            "where a.event.id = :eventId "
    )
    Long getEventRating(@Param("eventId") Long eventId);

    List<ReactionOnEvent> findByParticipantIdAndEventIdIn(Long userId, List<Long> eventsId);
}
