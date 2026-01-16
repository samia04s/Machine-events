package com.factory.machineevents.dto;

import java.time.Instant;

public class EventRequestDTO {

    public String eventId;
    public Instant eventTime;
    public String machineId;
    public long durationMs;
    public int defectCount;
}
