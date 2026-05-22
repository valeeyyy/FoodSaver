package datastructure;

import engine.MatchingEngine;
import model.FoodDonation;
import enums.ActionType;
import enums.DonationStatus;
import util.SystemConfig;

import java.util.*;

public class DonationPool {

    private final Queue<FoodDonation> queue;
    private final int yellowAlertMinutes;
    private final int redAlertMinutes;

    private AuditLog auditLog;
    private MatchingEngine engine;

    public DonationPool(AuditLog auditLog) {
        this.queue = new LinkedList<>();
        this.yellowAlertMinutes = SystemConfig.YELLOW_ALERT_MINUTES;
        this.redAlertMinutes = SystemConfig.FRESHNESS_BUFFER_MIN;
        this.auditLog = auditLog;
    }

    public void setEngine(MatchingEngine engine) {
        this.engine = engine;
    }

    public void enqueue(FoodDonation d) {
        queue.offer(d);
        System.out.println("  [Queue] Donasi " + d.getDonationId() + " masuk antrian (size=" + queue.size() + ")");
    }

    public FoodDonation dequeue() {
        return queue.poll();
    }

    public FoodDonation peek() {
        return queue.peek();
    }

    public FoodDonation peekEarliest() {
        return queue.stream()
                .min((a, b) -> a.getExpiredAt().compareTo(b.getExpiredAt()))
                .orElse(null);
    }

    public int purgeExpired() {
        List<FoodDonation> toRemove = new ArrayList<>();
        for (FoodDonation d : queue) {
            if (!d.isStillFresh()) {
                d.markAsExpired();
                toRemove.add(d);
            }
        }
        queue.removeAll(toRemove);
        return toRemove.size();
    }

    public void remove(FoodDonation d) {
        queue.remove(d);
    }

    public void checkAlerts() {
        boolean yellowFired = false;

        for (FoodDonation d : queue) {
            long remaining = d.getRemainingMinutes();

            if (d.isInBuffer()) {
                System.out.printf("[RED ALERT] Donasi %s sisa %d menit — dikeluarkan dari antrian!%n",
                        d.getDonationId(), remaining);
                d.markAsExpired();
                if (auditLog != null) {
                    auditLog.log("SYSTEM", ActionType.RED_ALERT, d.getDonationId(),
                            "Masuk buffer 30 menit, status EXPIRED_UNDELIVERED");
                }

            } else if (remaining <= yellowAlertMinutes && d.getStatus() == DonationStatus.WAITING) {
                System.out.printf("[YELLOW ALERT] Donasi %s sisa %d menit — jalankan ulang matching (radius 8km)%n",
                        d.getDonationId(), remaining);
                if (auditLog != null) {
                    auditLog.log("SYSTEM", ActionType.YELLOW_ALERT, d.getDonationId(),
                            "Sisa " + remaining + " menit, radius diperluas ke 8km");
                }
                yellowFired = true;
            }
        }

        queue.removeIf(d -> d.getStatus() == DonationStatus.EXPIRED_UNDELIVERED);

        if (yellowFired && engine != null) {
            engine.runWithExpandedRadius();
        }
    }

    public List<FoodDonation> getAll() {
        return new ArrayList<>(queue);
    }

    public List<FoodDonation> getAllSortedByExpiry() {
        List<FoodDonation> list = new ArrayList<>(queue);
        list.sort((a, b) -> a.getExpiredAt().compareTo(b.getExpiredAt()));
        return list;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}
