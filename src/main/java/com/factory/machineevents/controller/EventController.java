package com.factory.machineevents.controller;

import com.factory.machineevents.dto.BatchIngestResponseDTO;
import com.factory.machineevents.dto.EventRequestDTO;
import com.factory.machineevents.service.EventIngestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventIngestionService ingestionService;

    public EventController(EventIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Batch ingestion endpoint
     * POST /events/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<BatchIngestResponseDTO> ingestBatch(
            @RequestBody List<EventRequestDTO> events
    ) {

        if (events == null || events.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        BatchIngestResponseDTO response = ingestionService.ingestBatch(events);
        return ResponseEntity.ok(response);
    }
}
