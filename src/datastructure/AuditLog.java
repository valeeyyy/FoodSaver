package datastructure;

import model.AuditEntry;
import enums.ActionType;

import java.util.*;

public class AuditLog {

    private final LinkedList<AuditEntry> entries;

    public AuditLog() {
        this.entries = new LinkedList<>();
    }

    public void log(String actor, ActionType action, String targetId, String notes) {
        entries.addFirst(new AuditEntry(actor, action, targetId, notes));
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public int size() { 
        return entries.size(); 
    }
}