package model;

import enums.ActionType;
import enums.AlertType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import util.IdGenerator;

public class AuditEntry {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String entryId;
    private final LocalDateTime timestamp;
    private final String actor;

    private final Enum<?> eventType;

    private final String targetId;
    private final String notes;

    // Constructor 1: Jika yang masuk adalah ActionType
    public AuditEntry(String actor, ActionType actionType, String targetId, String notes) {
        this.entryId = IdGenerator.nextEntryId();
        this.timestamp = LocalDateTime.now();
        this.actor = actor;
        this.eventType = actionType;
        this.targetId = targetId;
        this.notes = notes;
    }

    // Constructor 2: Jika yang masuk adalah AlertType
    public AuditEntry(String actor, AlertType alertType, String targetId, String notes) {
        this.entryId = IdGenerator.nextEntryId();
        this.timestamp = LocalDateTime.now();
        this.actor = actor;
        this.eventType = alertType;
        this.targetId = targetId;
        this.notes = notes;
    }

    public static DateTimeFormatter getFmt() {
        return FMT;
    }

    public String getEntryId() {
        return entryId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getActor() {
        return actor;
    }

    public Enum<?> getEventType() {
        return eventType;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public String toString() {
        String typePrefix = (eventType instanceof AlertType) ? "ALERT: " : "ACTION: ";
        String eventName = typePrefix + eventType.name();

        return String.format("[%s] %s | %-18s | target=%-20s | %s",
                timestamp.format(FMT), actor, eventName, targetId, notes);
    }
}