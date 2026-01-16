package com.factory.machineevents.service;

import com.factory.machineevents.dto.MachineStatsResponseDTO;
import com.factory.machineevents.model.Event;
import com.factory.machineevents.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class EventStatsService {

    private static final double HEALTHY_THRESHOLD = 2.0;

    private final EventRepository eventRepository;

    public EventStatsService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public MachineStatsResponseDTO getStats(
            String machineId,
            Instant start,
            Instant end
    ) {

        List<Event> events =
                eventRepository.findEventsForMachineAndTimeRange(machineId, start, end);

        long eventsCount = events.size();

        long defectsCount = events.stream()
                .filter(e -> e.getDefectCount() >= 0)
                .mapToLong(Event::getDefectCount)
                .sum();

        double windowHours =
                Duration.between(start, end).toSeconds() / 3600.0;

        double avgDefectRate =
                windowHours > 0 ? defectsCount / windowHours : 0.0;

        String status =
                avgDefectRate < HEALTHY_THRESHOLD ? "Healthy" : "Warning";

        MachineStatsResponseDTO response = new MachineStatsResponseDTO();
        response.machineId = machineId;
        response.start = start;
        response.end = end;
        response.eventsCount = eventsCount;
        response.defectsCount = defectsCount;
        response.avgDefectRate = avgDefectRate;
        response.status = status;

        return response;
    }
}
