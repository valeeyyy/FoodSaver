package model;

import datastructure.AuditLog;
import datastructure.DeliveryHistory;
import datastructure.DonationPool;
import datastructure.ShelterRegistry;
import enums.AccountStatus;
import enums.ActionType;
import enums.DonationStatus;
import enums.OrderStatus;
import enums.ShelterType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Admin extends User {

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
        this.accountStatus = AccountStatus.APPROVED;
        this.pool = pool;
        this.registry = registry;
        this.history = history;
        this.auditLog = auditLog;
        this.userMap = userMap;
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

    // 3.2.11 — Data Structure: LinkedList digunakan agar akun pending
    // dapat ditambahkan dengan cepat (addFirst O(1)) sehingga akun terbaru
    // selalu tampil paling atas saat ditampilkan di dashboard.
    public LinkedList<User> viewPendingAccounts() {
        LinkedList<User> pending = new LinkedList<>();
        for (User u : userMap.values()) {
            if (u.getAccountStatus() == AccountStatus.PENDING) {
                pending.addFirst(u); // insert di head agar terbaru di atas
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

    public List<FoodDonation> filterDonationByStatus(DonationStatus status) {
        List<FoodDonation> result = new ArrayList<>();
        for (FoodDonation d : pool.getAll()) {
            if (d.getStatus() == status)
                result.add(d);
        }

        for (DeliveryOrder o : history.getAll()) {
            for (FoodDonation d : o.getBundle().getDonations()) {
                if (d.getStatus() == status)
                    result.add(d);
            }
        }
       
        for (FoodDonation d : history.getExpiredHistory()) {
            if (d.getStatus() == status)
                result.add(d);
        }

        for (FoodDonation d : history.getWastedHistory()) {
            if (d.getStatus() == status)
                result.add(d);
        }
        return result;
    }

    public Shelter searchShelterById(String id) {
        return registry.findById(id);
    }

    public List<Restaurant> searchRestaurantByName(String keyword) {
        List<Restaurant> result = new ArrayList<>();
        String lower = keyword.toLowerCase();
        for (User u : userMap.values()) {
            if (u instanceof Restaurant r && r.getName().toLowerCase().contains(lower))
                result.add(r);
        }
        return result;
    }

    public List<FoodDonation> searchDonationByName(String keyword) {
        List<FoodDonation> result = new ArrayList<>();
        String lower = keyword.toLowerCase();
        for (FoodDonation d : pool.getAll()) {
            if (d.getFoodName().toLowerCase().contains(lower))
                result.add(d);
        }
        for (DeliveryOrder o : history.getAll()) {
            for (FoodDonation d : o.getBundle().getDonations()) {
                if (d.getFoodName().toLowerCase().contains(lower))
                    result.add(d);
            }
        }
        return result;
    }

    public List<DeliveryOrder> filterOrdersByStatus(OrderStatus status) {
        return history.filterByStatus(status);
    }

    public List<AuditEntry> filterAuditLogByActor(String username) {
        return auditLog.filterByActor(username);
    }

    public void viewDashboard() {
        List<FoodDonation> activeDonations = pool.getAll();

        int totalActive  = activeDonations.size();
        int waitingCount = 0;
        int matchedCount = 0;

        for (FoodDonation d : activeDonations) {
            if (d.getStatus() == DonationStatus.WAITING) waitingCount++;
            if (d.getStatus() == DonationStatus.MATCHED) matchedCount++;
        }

        List<DeliveryOrder> allOrders = history.getAll();

        int deliveredCount   = 0;
        int deliveredPortions = 0;

        for (DeliveryOrder o : allOrders) {
            if (o.getStatus() == OrderStatus.DELIVERED) {
                deliveredCount++;
                deliveredPortions += o.getBundle().getTotalPortions() - o.getPortionSurplus();
            }
        }

        int expiredCount = history.getExpiredHistory().size();
        int wastedCount  = history.getWastedHistory().size();

        LinkedList<User> pendingAccounts = viewPendingAccounts();

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║           📊  DASHBOARD ADMIN — RINGKASAN        ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf("║  Donasi aktif di antrian   : %-20d║%n", totalActive);
        System.out.printf("║    ↳ Menunggu matching     : %-20d║%n", waitingCount);
        System.out.printf("║    ↳ Sudah dicocokkan      : %-20d║%n", matchedCount);
        System.out.println("║──────────────────────────────────────────────────║");
        System.out.printf("║  Order terselesaikan       : %-20d║%n", deliveredCount);
        System.out.printf("║  Porsi berhasil disalurkan : %-20d║%n", deliveredPortions);
        System.out.println("║──────────────────────────────────────────────────║");
        System.out.printf("║  Donasi expired (tdk sampai)  : %-15d║%n", expiredCount);
        System.out.printf("║  Donasi wasted (terbuang)     : %-15d║%n", wastedCount);
        System.out.println("║──────────────────────────────────────────────────║");
        System.out.printf("║  Akun menunggu verifikasi  : %-20d║%n", pendingAccounts.size());
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    public List<User> viewPendingEdits() {
        List<User> result = new ArrayList<>();
        for (User u : userMap.values()) {
            if (u.hasPendingEdit())
                result.add(u);
        }
        return result;
    }

    public void applyEditRequest(User user, String decision, String notes) {
        EditRequest req = user.getPendingEditRequest();
        if (req == null) return;

        if (decision.equalsIgnoreCase("APPROVED")) {
            req.approve(notes);
            Map<String, String> data = req.getNewData();

            if (data.containsKey("phone"))   user.setPhone(data.get("phone"));
            if (data.containsKey("address")) user.setAddress(data.get("address"));
            if (data.containsKey("password")) user.setPassword(data.get("password"));

            if (user instanceof Restaurant r) {
                if (data.containsKey("name"))     r.setName(data.get("name"));
                if (data.containsKey("owner"))    r.setOwnerName(data.get("owner"));
                if (data.containsKey("lat"))      r.setLat(Double.parseDouble(data.get("lat")));
                if (data.containsKey("lon"))      r.setLon(Double.parseDouble(data.get("lon")));
                if (data.containsKey("category")) r.setFoodCategory(data.get("category"));
            } else if (user instanceof Shelter s) {
                if (data.containsKey("name"))      s.setName(data.get("name"));
                if (data.containsKey("manager"))   s.setManagerName(data.get("manager"));
                if (data.containsKey("lat"))       s.setLat(Double.parseDouble(data.get("lat")));
                if (data.containsKey("lon"))       s.setLon(Double.parseDouble(data.get("lon")));
                if (data.containsKey("residents")) s.setResidents(Integer.parseInt(data.get("residents")));
                if (data.containsKey("type"))      s.setShelterType(ShelterType.valueOf(data.get("type")));
            }

            auditLog.log(username, ActionType.EDIT_APPROVED, user.getUserId(),
                    "Edit approved: " + notes);
            System.out.println("[✓] Perubahan data " + user.getUsername() + " telah disetujui dan diterapkan.");

        } else if (decision.equalsIgnoreCase("REJECTED")) {
            req.reject(notes);
            auditLog.log(username, ActionType.EDIT_REJECTED, user.getUserId(),
                    "Edit rejected: " + notes);
            System.out.println("[✗] Perubahan data " + user.getUsername() + " ditolak.");
        }
    }
}
