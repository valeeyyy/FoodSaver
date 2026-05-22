package model;

import datastructure.AuditLog;
import datastructure.DeliveryHistory;
import datastructure.DonationPool;
import datastructure.ShelterRegistry;
import enums.AccountStatus;
import enums.ActionType;
import enums.DonationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Admin — pengguna hak akses tertinggi, extends User.
 * Akun default dibuat otomatis saat startup (admin / admin123).
 * Semua dependensi disatukan ke dalam constructor.
 *
 * Fitur 3.2.2: verifyAccount(), viewPendingAccounts(), searchShelterById().
 * Fitur 3.2.5: viewDashboard() (status alert), viewActiveDonations(),
 *              viewUnmatchedDonations(), filterAuditLog().
 */
public class Admin extends User {

    private final int adminLevel;

    private final DonationPool    pool;
    private final ShelterRegistry registry;
    private final DeliveryHistory history;
    private final AuditLog        auditLog;

    /** Map<username, User> — dipakai viewPendingAccounts, akses O(1) per username */
    private final Map<String, User> userMap;

    public Admin(String username, String password,
                 DonationPool pool, ShelterRegistry registry,
                 DeliveryHistory history, AuditLog auditLog,
                 Map<String, User> userMap) {
        super(username, password, "—", "—");
        this.adminLevel = 1;
        this.accountStatus = AccountStatus.APPROVED;
        this.pool     = pool;
        this.registry = registry;
        this.history  = history;
        this.auditLog = auditLog;
        this.userMap  = userMap;
    }

    /**
     * Fitur 3.2.5 — Admin melihat status alert aktif di dashboard.
     * Output: YELLOW ALERT / RED ALERT jika ada donasi hampir expired.
     */
    public void viewDashboard() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║       DASHBOARD ADMIN — FoodSaver    ║");
        System.out.println("╚══════════════════════════════════════╝");

        boolean hasAlert = false;
        for (FoodDonation d : pool.getAll()) {
            long remaining = d.getRemainingMinutes();
            if (d.isInBuffer()) {
                System.out.println("[🔴 RED ALERT] Donasi " + d.getDonationId()
                        + " (Sisa: " + remaining + " mnt)");
                hasAlert = true;
            } else if (remaining <= 120 && d.getStatus() == DonationStatus.WAITING) {
                System.out.println("[🟡 YELLOW ALERT] Donasi " + d.getDonationId()
                        + " (Sisa: " + remaining + " mnt)");
                hasAlert = true;
            }
        }
        if (!hasAlert) {
            System.out.println("[✓] Tidak ada alert. Sistem berjalan normal.");
        }
        System.out.println();
    }

    /**
     * Fitur 3.2.2 — Verifikasi akun PENDING (APPROVED/REJECTED).
     * Output: status akun berubah, dicatat ke AuditLog.
     */
    public void verifyAccount(User user, String decision, String notes) {
        switch (decision.toUpperCase()) {
            case "APPROVED" -> {
                user.setAccountStatus(AccountStatus.APPROVED);
                System.out.println("[✓] Akun " + user.getUsername() + " disetujui.");
                auditLog.logAction(username, ActionType.APPROVE, user.getUserId(), notes);
            }
            case "REJECTED" -> {
                user.setAccountStatus(AccountStatus.REJECTED);
                System.out.println("[✗] Akun " + user.getUsername() + " ditolak.");
                auditLog.logAction(username, ActionType.REJECT, user.getUserId(), notes);
            }
            default -> System.out.println("[!] Keputusan tidak valid. Gunakan APPROVED / REJECTED / SKIP.");
        }
    }

    /**
     * Fitur 3.2.2 — Daftar akun berstatus PENDING yang menunggu verifikasi.
     * Iterasi HashMap values → ArrayList, tanpa Collectors.
     */
    public List<User> viewPendingAccounts() {
        List<User> pending = new ArrayList<>();
        for (User u : userMap.values()) {
            if (u.getAccountStatus() == AccountStatus.PENDING) {
                pending.add(u);
            }
        }
        return pending;
    }

    /**
     * Fitur 3.2.5 — Filter AuditLog berdasarkan donation/order ID.
     * Menelusuri perjalanan lengkap satu donasi.
     */
    public List<AuditEntry> filterAuditLog(String filter) {
        return auditLog.filterByDonationId(filter);
    }

    /**
     * Fitur 3.2.5 — Semua donasi aktif di antrian (Queue.getAll()).
     */
    public List<FoodDonation> viewActiveDonations() {
        return pool.getAll();
    }

    /**
     * Fitur 3.2.5 — Donasi WAITING yang belum berhasil dicocokkan.
     * Output: tampilan donasi tidak tersalurkan beserta sisa waktu.
     */
    public List<FoodDonation> viewUnmatchedDonations() {
        List<FoodDonation> result = new ArrayList<>();
        for (FoodDonation d : pool.getAll()) {
            if (d.getStatus() == DonationStatus.WAITING) {
                result.add(d);
            }
        }
        return result;
    }

    /**
     * Fitur 3.2.2 — Pencarian panti O(1) via HashMap di ShelterRegistry.
     */
    public Shelter searchShelterById(String id) {
        return registry.findById(id);
    }

    public int getAdminLevel() { return adminLevel; }
}
