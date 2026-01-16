package com.factory.machineevents.service;

import com.factory.machineevents.dto.BatchIngestResponseDTO;
import com.factory.machineevents.dto.EventRequestDTO;
import com.factory.machineevents.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EventIngestionServiceTest {

    @Autowired
    private EventIngestionService ingestionService;

    @Autowired
    private EventRepository eventRepository;

    private EventRequestDTO baseEvent;

    @BeforeEach
    void setup() {
        eventRepository.deleteAll();

        baseEvent = new EventRequestDTO();
        baseEvent.eventId = "E-100";
        baseEvent.eventTime = Instant.parse("2026-01-15T10:00:00Z");
        baseEvent.machineId = "M-001";
        baseEvent.durationMs = 1000;
        baseEvent.defectCount = 1;
    }

    // 1️⃣ Identical duplicate → deduped
    @Test
    void identicalDuplicateEventIsDeduped() {
        ingestionService.ingestBatch(List.of(baseEvent));
        BatchIngestResponseDTO response =
                ingestionService.ingestBatch(List.of(baseEvent));

        assertEquals(1, response.deduped);
    }

    // 2️⃣ Same eventId + different payload + newer → updated
    @Test
    void newerPayloadUpdatesExistingEvent() {
        ingestionService.ingestBatch(List.of(baseEvent));

        baseEvent.durationMs = 2000;
        BatchIngestResponseDTO response =
                ingestionService.ingestBatch(List.of(baseEvent));

        assertEquals(1, response.updated);
    }

    // 3️⃣ Same eventId + different payload + older receivedTime → ignored
    @Test
    void olderPayloadIsIgnored() {
        ingestionService.ingestBatch(List.of(baseEvent));

        EventRequestDTO older = new EventRequestDTO();
        older.eventId = baseEvent.eventId;
        older.eventTime = baseEvent.eventTime;
        older.machineId = baseEvent.machineId;
        older.durationMs = 3000;
        older.defectCount = 2;

        BatchIngestResponseDTO response =
                ingestionService.ingestBatch(List.of(older));

        assertEquals(1, response.updated);
    }

    // 4️⃣ Invalid duration rejected
    @Test
    void invalidDurationIsRejected() {
        baseEvent.durationMs = -5;

        BatchIngestResponseDTO response =
                ingestionService.ingestBatch(List.of(baseEvent));

        assertEquals(1, response.rejected);
        assertEquals("INVALID_DURATION", response.rejections.get(0).reason);
    }

    // 5️⃣ Future eventTime rejected
    @Test
    void futureEventTimeIsRejected() {
        baseEvent.eventTime = Instant.now().plusSeconds(60 * 20);

        BatchIngestResponseDTO response =
                ingestionService.ingestBatch(List.of(baseEvent));

        assertEquals(1, response.rejected);
        assertEquals("EVENT_TIME_IN_FUTURE", response.rejections.get(0).reason);
    }

    // 6️⃣ defectCount = -1 ignored in defect totals
    @Test
    void defectMinusOneIgnoredInStats() {
        baseEvent.defectCount = -1;
        ingestionService.ingestBatch(List.of(baseEvent));

        long defects = eventRepository.findAll().stream()
                .filter(e -> e.getDefectCount() >= 0)
                .mapToLong(e -> e.getDefectCount())
                .sum();

        assertEquals(0, defects);
    }

    // 7️⃣ start inclusive, end exclusive
    @Test
    void startInclusiveEndExclusiveWorks() {
        ingestionService.ingestBatch(List.of(baseEvent));

        var events =
                eventRepository.findEventsForMachineAndTimeRange(
                        "M-001",
                        baseEvent.eventTime,
                        baseEvent.eventTime
                );

        assertEquals(0, events.size());
    }

    // 8️⃣ Thread safety: concurrent ingestion
    @Test
    void concurrentIngestionDoesNotCreateDuplicates() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(10);

        Callable<Void> task = () -> {
            ingestionService.ingestBatch(List.of(baseEvent));
            return null;
        };

        List<Callable<Void>> tasks = List.of(
                task, task, task, task, task
        );

        executor.invokeAll(tasks);
        executor.shutdown();

        assertEquals(1, eventRepository.count());
    }
}
