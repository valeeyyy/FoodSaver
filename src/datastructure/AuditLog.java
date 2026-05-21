package datastructure;

import enums.ActionType;
import enums.AlertType;
import java.util.*;
import model.AuditEntry;

public class AuditLog {

    private final LinkedList<AuditEntry> entries;

    public AuditLog() {
        this.entries = new LinkedList<>();
    }

    public void logAction(String actor, ActionType action, String targetId, String notes) {
        entries.addFirst(new AuditEntry(actor, action, targetId, notes));
    }

    public void logAlert(String actor, AlertType alert, String targetId, String notes) {
        entries.addFirst(new AuditEntry(actor, alert, targetId, notes));
    }
}