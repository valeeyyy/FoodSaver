package model;

import datastructure.AuditLog;
import datastructure.DonationPool;
import datastructure.ShelterRegistry;
import enums.AccountStatus;
import enums.ActionType;
import enums.DonationStatus;
import java.util.*;

public class Admin extends User {

    private ShelterRegistry registry;
    private AuditLog auditLog;
    private List<User> allUsers;

    public Admin(String username, String password, ShelterRegistry registry, AuditLog auditLog, List<User> allUsers) {
        super(username, password, "—", "—");
        this.accountStatus = AccountStatus.APPROVED;
        this.registry = registry;
        this.auditLog = auditLog;
        this.allUsers = allUsers;
    }

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

    public List<User> viewPendingAccounts() {
        List<User> result = new ArrayList<>();
        for (User u : allUsers) {
            if (u.getAccountStatus() == AccountStatus.PENDING) {
                result.add(u);
            }
        }
        return result;
    }

    public List<User> viewAllAccounts() {
        List<User> result = new ArrayList<>();
        for (User u : allUsers) {
            if (!(u instanceof Admin)) {
                result.add(u);
            }
        }
        return result;
    }

    public List<User> viewAccountsByStatus(AccountStatus status) {
        List<User> result = new ArrayList<>();
        for (User u : allUsers) {
            if (!(u instanceof Admin) && u.getAccountStatus() == status) {
                result.add(u);
            }
        }
        return result;
    }

    public boolean deleteAccount(User user) {
        boolean removed = allUsers.remove(user);
        if (removed) {
            if (user instanceof Shelter s) {
                registry.remove(s);
            }
            auditLog.logAction(username, ActionType.DELETE, user.getUserId(), "Akun dihapus: " + user.getUsername());
            System.out.println("[✓] Akun " + user.getUsername() + " berhasil dihapus.");
        }
        return removed;
    }

    public Shelter searchShelterById(String id, ShelterRegistry registry) {
        return registry.findById(id);
    }

    public List<FoodDonation> viewUnmatchedDonations(DonationPool pool) {
        List<FoodDonation> unmatched = new ArrayList<>();
        List<FoodDonation> allActive = pool.getAll();

        for (FoodDonation d : allActive) {
            if (d.getStatus() == DonationStatus.WAITING) {
                unmatched.add(d);
            }
        }
        return unmatched;
    }
}