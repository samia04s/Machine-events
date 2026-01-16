package com.factory.machineevents.controller;

import com.factory.machineevents.dto.MachineStatsResponseDTO;
import com.factory.machineevents.service.EventStatsService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/stats")
public class EventStatsController {

    private final EventStatsService statsService;

    public EventStatsController(EventStatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * GET /stats?machineId=...&start=...&end=...
     */
    @GetMapping
    public MachineStatsResponseDTO getStats(
            @RequestParam String machineId,
            @RequestParam Instant start,
            @RequestParam Instant end
    ) {
        return statsService.getStats(machineId, start, end);
    }
}
