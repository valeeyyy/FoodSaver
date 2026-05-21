package datastructure;

import engine.MatchingEngine;
import model.FoodDonation;
import model.DonationBundle;
import enums.ActionType;
import enums.AlertType;
import enums.DonationStatus;
import util.SystemConfig;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Comparator;

public class DonationPool {

    private Queue<FoodDonation> queue;
    private int yellowAlertMinutes;
    private int redAlertMinutes;

    private AuditLog auditLog;
    private MatchingEngine engine;

    public DonationPool() {
        this.queue = new LinkedList<>();
        this.yellowAlertMinutes = SystemConfig.YELLOW_ALERT_MINUTES;
        this.redAlertMinutes = SystemConfig.FRESHNESS_BUFFER_MIN;
    }

    public void injectDependencies(AuditLog auditLog, MatchingEngine engine) {
        this.auditLog = auditLog;
        this.engine = engine;
    }

    public void enqueue(FoodDonation d) {
        queue.offer(d);
        System.out.println("Donasi " + d.getDonationId() + " masuk antrian (size=" + queue.size() + ")");
    }

    public FoodDonation dequeue() {
        return queue.poll();
    }

    public FoodDonation peek() {
        return queue.peek();
    }

    public FoodDonation peekEarliest() {
        if (queue.isEmpty()) {
            return null;
        }

        FoodDonation earliest = null;
        
        for (FoodDonation d : queue) {
            if (earliest == null) {
                earliest = d;
            } else if (d.getExpiredAt().isBefore(earliest.getExpiredAt())) {
                earliest = d;
            }
        }
        
        return earliest;
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
        List<FoodDonation> toRemove = new ArrayList<>();

        for (FoodDonation d : queue) {
            long remaining = d.getRemainingMinutes();

            if (d.isInBuffer()) {
                System.out.println("[RED ALERT] Donasi " + d.getDonationId() + " sisa " + remaining + " menit");
                d.markAsExpired();
                
                if (auditLog != null) {
                    auditLog.logAlert("SYSTEM", AlertType.RED_ALERT, d.getDonationId(), "Masuk buffer 30 menit");
                }
                
                toRemove.add(d);

            } else if (remaining <= yellowAlertMinutes && d.getStatus() == DonationStatus.WAITING) {
                System.out.println("[YELLOW ALERT] Donasi " + d.getDonationId() + " sisa " + remaining + " menit");
                
                if (auditLog != null) {
                    auditLog.logAlert("SYSTEM", AlertType.YELLOW_ALERT, d.getDonationId(), "Radius diperluas ke 8km");
                }
                
                yellowFired = true;
            }
        }

        queue.removeAll(toRemove);

        if (yellowFired && engine != null) {
            engine.runWithExpandedRadius();
        }
    }

    public List<FoodDonation> getAll() {
        return new ArrayList<>(queue);
    }

    public List<FoodDonation> getAllSortedByExpiry() {
        List<FoodDonation> list = new ArrayList<>(queue);
        
        list.sort(new Comparator<FoodDonation>() {
            @Override
            public int compare(FoodDonation a, FoodDonation b) {
                return a.getExpiredAt().compareTo(b.getExpiredAt());
            }
        });
        
        return list;
    }

    public boolean isEmpty() { 
        return queue.isEmpty(); 
    }
    
    public int size() { 
        return queue.size(); 
    }

    public List<DonationBundle> generateAllPossibleBundles() {
        List<DonationBundle> result = new ArrayList<>();
        List<FoodDonation> list = getAll(); 
        
        int n = list.size();
        
        if (n > 10) {
            n = 10; 
        }

        int totalCombinations = (int) Math.pow(2, n);

        for (int i = 1; i < totalCombinations; i++) {
            DonationBundle bundle = new DonationBundle();
            
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) {
                    bundle.addDonation(list.get(j));
                }
            }
            
            result.add(bundle);
        }

        return result;
    }
}
