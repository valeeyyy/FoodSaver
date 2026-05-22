package datastructure;

import model.AuditEntry;
import enums.ActionType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * AuditLog — wrapper LinkedList yang mencatat semua aksi yang mengubah data sistem.
 * Insert di head → O(1), entri terbaru selalu pertama.
 *
 * Fitur 3.2.2: mencatat REGISTER, APPROVE, REJECT.
 * Fitur 3.2.5: mencatat YELLOW_ALERT, RED_ALERT, STARTUP_CLEANUP, EXPIRE, WASTED.
 */
public class AuditLog {

    /** LinkedList — insert di head O(1) */
    private final LinkedList<AuditEntry> entries;

    public AuditLog() {
        this.entries = new LinkedList<>();
    }

    /** Tambahkan entri baru ke log — dipanggil otomatis oleh sistem */
    public void log(String actor, ActionType action, String targetId, String notes) {
        entries.addFirst(new AuditEntry(actor, action, targetId, notes));
    }

    /**
     * Fitur 3.2.5 — Filter log berdasarkan donation/order ID.
     * Menelusuri perjalanan lengkap satu donasi dari awal sampai akhir.
     */
    public List<AuditEntry> filterByDonationId(String id) {
        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry e : entries) {
            if (e.getTargetId().contains(id)) result.add(e);
        }
        return result;
    }

    /** Filter berdasarkan actor (user ID atau "SYSTEM") */
    public List<AuditEntry> filterByActor(String userId) {
        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry e : entries) {
            if (e.getActor().equals(userId)) result.add(e);
        }
        return result;
    }

    /**
     * Fitur 3.2.5 — Hanya tampilkan entri EXPIRE dan WASTED.
     * Output: AuditLog berisi catatan kejadian alert dan pembersihan.
     */
    public List<AuditEntry> filterExpiredAndWasted() {
        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry e : entries) {
            if (e.getActionType() == ActionType.EXPIRE
                    || e.getActionType() == ActionType.WASTED) result.add(e);
        }
        return result;
    }

    /** Seluruh log */
    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public int size() {
        return entries.size();
    }
}
