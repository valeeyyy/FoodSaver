package datastructure;

import engine.MatchingEngine;
import model.FoodDonation;
import model.Notification;
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
    private DeliveryHistory history;

    public DonationPool(AuditLog auditLog) {
        this.queue = new LinkedList<>();
        this.yellowAlertMinutes = SystemConfig.YELLOW_ALERT_MINUTES;
        this.redAlertMinutes = SystemConfig.FRESHNESS_BUFFER_MIN;
        this.auditLog = auditLog;
    }

    public void setEngine(MatchingEngine engine) {
        this.engine = engine;
    }

    public void setHistory(DeliveryHistory history) {
        this.history = history;
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

    public void remove(FoodDonation d) {
        queue.remove(d);
    }

    public void checkAlerts() {
        boolean yellowFired = false;
        List<FoodDonation> toExpire = new ArrayList<>();

        for (FoodDonation d : queue) {
            long remaining = d.getRemainingMinutes();

            if (d.isInBuffer()) {
                Notification red = Notification.createRedAlert(d.getDonationId());
                red.display();
                if (auditLog != null) {
                    auditLog.log(red.toAuditEntry());
                }
                d.markAsExpired();
                toExpire.add(d);

            } else if (remaining <= yellowAlertMinutes && d.getStatus() == DonationStatus.WAITING
                    && !d.isYellowAlertFired()) {
                d.setYellowAlertFired(true);
                Notification yellow = Notification.createYellowAlert(d.getDonationId());
                yellow.display();
                if (auditLog != null) {
                    auditLog.log(yellow.toAuditEntry());
                }
                yellowFired = true;
            }
        }

        for (FoodDonation d : toExpire) {
            queue.remove(d);
            if (history != null) {
                history.addExpired(d);
            }
        }

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
