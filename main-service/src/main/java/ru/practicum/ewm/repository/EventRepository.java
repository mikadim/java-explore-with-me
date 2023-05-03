package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventStatus;

import java.time.LocalDateTime;
import java.util.*;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("select a from Event a " +
            "where a.id in (?1) " +
            "order by a.eventDate asc"
    )
    List<Event> getEventsFromSet(Set<Long> eventsId);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    @Query("select a as event, (count(b.id) - count(c.id)) as rating from Event a " +
            "left join ReactionOnEvent b on b.event = a and b.reaction = 'LIKE' " +
            "left join ReactionOnEvent c on c.event = a and c.reaction = 'DISLIKE' " +
            "where a.id = :eventId and a.state = :state " +
            "group by a"
    )
    Optional<EventWithRating> findByIdAndState(Long eventId, EventStatus state);

    @Query("select a as event, (count(b.id) - count(c.id)) as rating from Event a " +
            "left join ReactionOnEvent b on b.event = a and b.reaction = 'LIKE' " +
            "left join ReactionOnEvent c on c.event = a and c.reaction = 'DISLIKE' " +
            "where (:#{#userIds == null} = true or a.initiator.id in :userIds) " +
            "and (:#{#categories == null} = true or a.category.id in :categories) " +
            "and (:#{#eventStatuses == null} = true or a.state in :eventStatuses) " +
            "and ( " +
            "(:#{#rangeStart != null} = true and :#{#rangeEnd != null} = true and a.eventDate between :rangeStart and :rangeEnd) " +
            "or ( " +
            "(:#{#rangeStart == null} = true or a.eventDate >= :rangeStart) and (:#{#rangeEnd == null} = true or a.eventDate <= :rangeEnd)) " +
            ") " +
            "and (:#{#eventId == null} = true or a.id = :eventId) " +
            "group by a"
    )
    Page<EventWithRating> getEventsForPrivateUsers(@Param("userIds") List<Long> userIds, @Param("eventStatuses") List<EventStatus> eventStatuses,
                                         @Param("categories") List<Integer> categories, @Param("rangeStart") LocalDateTime rangeStart,
                                         @Param("rangeEnd") LocalDateTime rangeEnd, @Param("eventId") Long eventId, Pageable page);


    @Query("select a from Event a " +
            "where a.state = 'PUBLISHED' " +
            "and (:#{#categories == null} = true or a.category.id in :categories) " +
            "and (:#{#texts == null} = true " +
            "or (lower(a.annotation) like concat('%', :texts, '%') or lower(a.description) like concat('%', :texts, '%')) " +
            ") " +
            "and (:#{#paid == null} = true or a.paid = :paid) " +
            "and ( " +
            "(:#{#rangeStart != null} = true and :#{#rangeEnd != null} = true and a.eventDate between :rangeStart and :rangeEnd) " +
            "or ( " +
            "(:#{#rangeStart == null} = true or a.eventDate >= :rangeStart) and (:#{#rangeEnd == null} = true or a.eventDate <= :rangeEnd)) " +
            ") " +
            "and (:#{#onlyAvailable == false} = true or a.confirmedRequests < a.participantLimit)"
    )
    Page<Event> getEventsForPublicUsers(@Param("texts") String texts, @Param("categories") List<Integer> categories,
                                        @Param("paid") Boolean paid, @Param("rangeStart") LocalDateTime rangeStart,
                                        @Param("rangeEnd") LocalDateTime rangeEnd, @Param("onlyAvailable") Boolean onlyAvailable,
                                        Pageable page);

    @Query("select a as event, (count(b.id) - count(c.id)) as rating from Event a " +
            "left join ReactionOnEvent b on b.event = a and b.reaction = 'LIKE' " +
            "left join ReactionOnEvent c on c.event = a and c.reaction = 'DISLIKE' " +
            "where a.eventDate > :startTime and a.state = :state " +
            "group by a"
    )
    Page<EventWithRating> findByEventDateGreaterThanAndState(@Param("startTime") LocalDateTime startTime, @Param("state") EventStatus state, Pageable page);

    @Query("select a as event, (count(b.id) - count(c.id)) as rating from Event a " +
            "left join ReactionOnEvent b on b.event = a and b.reaction = 'LIKE' " +
            "left join ReactionOnEvent c on c.event = a and c.reaction = 'DISLIKE' " +
            "where a.id = :eventId and a.initiator.id = :userId " +
            "group by a"
    )
    Optional<EventWithRating> findByIdAndInitiatorIdWithRating(@Param("eventId") Long eventId, @Param("userId") Long userId);

    interface EventWithRating {
        Event getEvent();

        Long getRating();
    }
}