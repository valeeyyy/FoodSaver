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
                    || e.getActionType() == ActionType.WASTED)
                result.add(e);
        }
        return result;
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public int size() {
        return entries.size();
    }
}
