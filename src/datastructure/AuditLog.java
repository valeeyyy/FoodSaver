package datastructure;

import model.AuditEntry;
import enums.ActionType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AuditLog {

    private final LinkedList<AuditEntry> entries;

    public AuditLog() {
        this.entries = new LinkedList<>();
    }

    public void log(String actor, ActionType action, String targetId, String notes) {
        entries.addFirst(new AuditEntry(actor, action, targetId, notes));
    }

    public void log(AuditEntry entry) {
        entries.addFirst(entry);
    }

    public List<AuditEntry> filterByDonationId(String id) {
        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry e : entries) {
            if (e.getTargetId().contains(id))
                result.add(e);
        }
        return result;
    }

    public List<AuditEntry> filterByActor(String userId) {
        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry e : entries) {
            if (e.getActor().equals(userId))
                result.add(e);
        }
        return result;
    }

    public List<AuditEntry> filterExpiredAndWasted() {
        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry e : entries) {
            if (e.getActionType() == ActionType.EXPIRE
                    || e.getActionType() == ActionType.WASTED
                    || e.getActionType() == ActionType.RED_ALERT
                    || e.getActionType() == ActionType.YELLOW_ALERT)
                result.add(e);
        }
        return result;
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> traceDonationLifecycle(String donationId) {
        List<AuditEntry> trace = filterByDonationId(donationId);
        trace.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
        return trace;
    }

    public int size() {
        return entries.size();
    }
}
