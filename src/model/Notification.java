package model;

import enums.ActionType;
import enums.AlertType;
import util.IdGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String message;
    private final AlertType alertType;
    private final LocalDateTime timestamp;
    private final String targetId;

    private Notification(String message, AlertType alertType, String targetId) {
        this.message = message;
        this.alertType = alertType;
        this.timestamp = LocalDateTime.now();
        this.targetId = targetId;
    }

    public static Notification createYellowAlert(String donationId) {
        return new Notification(
                "[🟡 YELLOW ALERT] Donasi " + donationId
                        + " hampir expired — radius matching diperluas ke 8 km.",
                AlertType.YELLOW_ALERT,
                donationId);
    }

    public static Notification createRedAlert(String donationId) {
        return new Notification(
                "[🔴 RED ALERT] Donasi " + donationId
                        + " masuk buffer 30 menit — dikeluarkan dari antrian, status EXPIRED_UNDELIVERED.",
                AlertType.RED_ALERT,
                donationId);
    }

    public static Notification createMatchFound(String orderId) {
        return new Notification(
                "[✓ MATCH] Order " + orderId + " berhasil dibuat.",
                AlertType.MATCH_FOUND,
                orderId);
    }

    public static Notification createStartupCleanup(String donationId) {
        return new Notification(
                "[STARTUP] Donasi " + donationId + " dari sesi sebelumnya dipindahkan ke riwayat (WASTED).",
                AlertType.STARTUP_CLEANUP,
                donationId);
    }

    public void display() {
        System.out.printf("[%s] %s%n", timestamp.format(FMT), message);
    }

    public AuditEntry toAuditEntry() {
        ActionType action = switch (alertType) {
            case YELLOW_ALERT -> ActionType.YELLOW_ALERT;
            case RED_ALERT -> ActionType.RED_ALERT;
            case MATCH_FOUND -> ActionType.MATCH;
            case STARTUP_CLEANUP -> ActionType.STARTUP_CLEANUP;
        };
        return new AuditEntry("SYSTEM", action, targetId, message);
    }

    public String getMessage() {
        return message;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getTargetId() {
        return targetId;
    }
}
