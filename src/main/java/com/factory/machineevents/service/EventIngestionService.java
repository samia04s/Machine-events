package com.factory.machineevents.service;

import com.factory.machineevents.dto.BatchIngestResponseDTO;
import com.factory.machineevents.dto.EventRequestDTO;
import com.factory.machineevents.model.Event;
import com.factory.machineevents.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class EventIngestionService {

    private static final long MAX_DURATION_MS = Duration.ofHours(6).toMillis();
    private static final Duration FUTURE_EVENT_LIMIT = Duration.ofMinutes(15);

    private final EventRepository eventRepository;

    public EventIngestionService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public BatchIngestResponseDTO ingestBatch(List<EventRequestDTO> events) {

        BatchIngestResponseDTO response = new BatchIngestResponseDTO();
        Instant now = Instant.now();

        for (EventRequestDTO dto : events) {

            // 1️⃣ Validation
            String validationError = validate(dto, now);
            if (validationError != null) {
                response.rejected++;
                response.rejections.add(
                        new BatchIngestResponseDTO.RejectionDTO(dto.eventId, validationError)
                );
                continue;
            }

            // 2️⃣ Check existing event
            Optional<Event> existingOpt = eventRepository.findByEventId(dto.eventId);

            if (existingOpt.isEmpty()) {
                // 3️⃣ New Event
                Event event = mapToEntity(dto, now);
                eventRepository.save(event);
                response.accepted++;
            } else {
                Event existing = existingOpt.get();

                if (isSamePayload(existing, dto)) {
                    // 4️⃣ Exact duplicate
                    response.deduped++;
                } else {
                    // 5️⃣ Update or ignore based on receivedTime
                    if (now.isAfter(existing.getReceivedTime())) {
                        updateEntity(existing, dto, now);
                        eventRepository.save(existing);
                        response.updated++;
                    } else {
                        response.deduped++;
                    }
                }
            }
        }

        return response;
    }

    // ================= Helper Methods =================

    private String validate(EventRequestDTO dto, Instant now) {

        if (dto.durationMs < 0 || dto.durationMs > MAX_DURATION_MS) {
            return "INVALID_DURATION";
        }

        if (dto.eventTime.isAfter(now.plus(FUTURE_EVENT_LIMIT))) {
            return "EVENT_TIME_IN_FUTURE";
        }

        return null;
    }

    private Event mapToEntity(EventRequestDTO dto, Instant receivedTime) {
        Event event = new Event();
        event.setEventId(dto.eventId);
        event.setEventTime(dto.eventTime);
        event.setMachineId(dto.machineId);
        event.setDurationMs(dto.durationMs);
        event.setDefectCount(dto.defectCount);
        event.setReceivedTime(receivedTime);
        return event;
    }

    private void updateEntity(Event event, EventRequestDTO dto, Instant receivedTime) {
        event.setEventTime(dto.eventTime);
        event.setMachineId(dto.machineId);
        event.setDurationMs(dto.durationMs);
        event.setDefectCount(dto.defectCount);
        event.setReceivedTime(receivedTime);
    }

    private boolean isSamePayload(Event event, EventRequestDTO dto) {
        return event.getEventTime().equals(dto.eventTime)
                && event.getMachineId().equals(dto.machineId)
                && event.getDurationMs() == dto.durationMs
                && event.getDefectCount() == dto.defectCount;
    }
}
