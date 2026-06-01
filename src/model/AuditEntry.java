package model;

import enums.ActionType;
import util.IdGenerator;
import enums.DonationStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditEntry {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String entryId;
    private final LocalDateTime timestamp;
    private final String actor;
    private final ActionType actionType;
    private final String targetId;
    private final String notes;
    private final DonationStatus finalStatus;

    public AuditEntry(String actor, ActionType actionType, String targetId, String notes, DonationStatus finalStatus) {
        this.entryId = IdGenerator.nextEntryId();
        this.timestamp = LocalDateTime.now();
        this.actor = actor;
        this.actionType = actionType;
        this.targetId = targetId;
        this.notes = notes;
        this.finalStatus = finalStatus;
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

    public ActionType getActionType() {
        return actionType;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getNotes() {
        return notes;
    }

    public DonationStatus getFinalStatus() {
        return finalStatus;
    }

    @Override
    public String toString() {
        String statusPart = (finalStatus != null) ? " | finalStatus=" + finalStatus : "";
        return String.format("[%s] %s | %-15s | target=%-20s | %s%s",
                timestamp.format(FMT), actor, actionType, targetId, notes, statusPart);
    }
}