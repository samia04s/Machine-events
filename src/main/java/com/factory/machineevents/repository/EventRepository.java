package com.factory.machineevents.repository;

import com.factory.machineevents.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Used for deduplication & updates
    Optional<Event> findByEventId(String eventId);

    // Used for machine stats query
    @Query("""
        SELECT e FROM Event e
        WHERE e.machineId = :machineId
          AND e.eventTime >= :start
          AND e.eventTime < :end
    """)
    List<Event> findEventsForMachineAndTimeRange(
            @Param("machineId") String machineId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    // Used for global stats / top defect lines (later)
    @Query("""
        SELECT e FROM Event e
        WHERE e.eventTime >= :start
          AND e.eventTime < :end
    """)
    List<Event> findEventsInTimeRange(
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}
