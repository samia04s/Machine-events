package com.factory.machineevents.dto;

import java.util.ArrayList;
import java.util.List;

public class BatchIngestResponseDTO {

    public int accepted;
    public int updated;
    public int deduped;
    public int rejected;

    public List<RejectionDTO> rejections = new ArrayList<>();

    public static class RejectionDTO {
        public String eventId;
        public String reason;

        public RejectionDTO(String eventId, String reason) {
            this.eventId = eventId;
            this.reason = reason;
        }
    }
}
