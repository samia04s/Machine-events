package com.factory.machineevents.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "events",
        indexes = {
                @Index(name = "idx_event_event_time", columnList = "eventTime"),
                @Index(name = "idx_event_machine_id", columnList = "machineId")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_event_id", columnNames = "eventId")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String eventId;

    @Column(nullable = false)
    private Instant eventTime;

    @Column(nullable = false)
    private Instant receivedTime;

    @Column(nullable = false)
    private String machineId;

    @Column(nullable = false)
    private long durationMs;

    @Column(nullable = false)
    private int defectCount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
