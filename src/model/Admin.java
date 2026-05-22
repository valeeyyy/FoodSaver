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

public class Admin extends User {

    private final int adminLevel;

    private final DonationPool pool;
    private final ShelterRegistry registry;
    private final DeliveryHistory history;
    private final AuditLog auditLog;

    private final Map<String, User> userMap;

    public Admin(String username, String password,
            DonationPool pool, ShelterRegistry registry,
            DeliveryHistory history, AuditLog auditLog,
            Map<String, User> userMap) {
        super(username, password, "—", "—");
        this.adminLevel = 1;
        this.accountStatus = AccountStatus.APPROVED;
        this.pool = pool;
        this.registry = registry;
        this.history = history;
        this.auditLog = auditLog;
        this.userMap = userMap;
    }

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

    public void verifyAccount(User user, String decision, String notes) {
        switch (decision.toUpperCase()) {
            case "APPROVED" -> {
                user.setAccountStatus(AccountStatus.APPROVED);
                System.out.println("[✓] Akun " + user.getUsername() + " disetujui.");
                auditLog.log(username, ActionType.APPROVE, user.getUserId(), notes);
            }
            case "REJECTED" -> {
                user.setAccountStatus(AccountStatus.REJECTED);
                System.out.println("[✗] Akun " + user.getUsername() + " ditolak.");
                auditLog.log(username, ActionType.REJECT, user.getUserId(), notes);
            }
            default -> System.out.println("[!] Keputusan tidak valid. Gunakan APPROVED / REJECTED / SKIP.");
        }
    }

    public List<User> viewPendingAccounts() {
        List<User> pending = new ArrayList<>();
        for (User u : userMap.values()) {
            if (u.getAccountStatus() == AccountStatus.PENDING) {
                pending.add(u);
            }
        }
        return pending;
    }

    public List<AuditEntry> filterAuditLog(String filter) {
        return auditLog.filterByDonationId(filter);
    }

    public List<FoodDonation> viewActiveDonations() {
        return pool.getAll();
    }

    public List<FoodDonation> viewUnmatchedDonations() {
        List<FoodDonation> result = new ArrayList<>();
        for (FoodDonation d : pool.getAll()) {
            if (d.getStatus() == DonationStatus.WAITING) {
                result.add(d);
            }
        }
        return result;
    }

    public Shelter searchShelterById(String id) {
        return registry.findById(id);
    }

    public int getAdminLevel() {
        return adminLevel;
    }
}
