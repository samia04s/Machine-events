package com.factory.machineevents.dto;

import java.time.Instant;

public class MachineStatsResponseDTO {

    public String machineId;
    public Instant start;
    public Instant end;

    public long eventsCount;
    public long defectsCount;
    public double avgDefectRate;
    public String status;
}
