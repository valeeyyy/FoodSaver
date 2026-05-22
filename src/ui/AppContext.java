package ui;

import datastructure.*;
import engine.MatchingEngine;
import model.*;
import enums.ActionType;
import enums.DonationStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppContext {

    public final DonationPool pool;
    public final ShelterRegistry registry;
    public final FoodExpiryTree expiryTree;
    public final DeliveryHistory history;
    public final AuditLog auditLog;

    public final Map<String, User> userMap;

    public final MatchingEngine engine;
    public final Admin admin;

    public AppContext() {
        registry = new ShelterRegistry();
        expiryTree = new FoodExpiryTree();
        history = new DeliveryHistory();
        auditLog = new AuditLog();
        userMap = new HashMap<>();

        pool = new DonationPool(auditLog);
        engine = new MatchingEngine(pool, registry, expiryTree, history, auditLog);
        pool.setEngine(engine);

        admin = new Admin("admin", "admin123", pool, registry, history, auditLog, userMap);
        userMap.put(admin.getUsername(), admin);
    }

    public void startup() {
        System.out.println("=== FOODSAVER STARTUP ===");

        for (Shelter s : registry.getAll()) {
            s.resetDailyPortions();
        }

        List<FoodDonation> toClean = new ArrayList<>();
        for (FoodDonation d : pool.getAll()) {
            if (d.getStatus() == DonationStatus.EXPIRED_UNDELIVERED) {
                d.markAsWasted();
                toClean.add(d);
                auditLog.log("SYSTEM", ActionType.STARTUP_CLEANUP,
                        d.getDonationId(), "Dipindahkan ke history sebagai WASTED saat startup");
            }
        }
        for (FoodDonation d : toClean) {
            pool.remove(d);
            expiryTree.remove(d);
            history.addWasted(d);
        }

        if (!toClean.isEmpty()) {
            System.out.println("[!] " + toClean.size() + " donasi expired dipindahkan ke riwayat (WASTED).");
        }

        System.out.println("[✓] Startup selesai. Akun admin default: admin / admin123\n");
    }

    public void submitDonation(FoodDonation d) {
        pool.enqueue(d);
        expiryTree.insert(d);
        auditLog.log(d.getRestaurant().getUsername(), ActionType.POST,
                d.getDonationId(), "Posted: " + d.getFoodName());
        System.out.println("\n[MatchingEngine] Donasi baru masuk — menjalankan matching...");
        engine.run();
    }

    public User findUser(String username, String password) {
        User u = userMap.get(username);
        if (u != null && u.getPassword().equals(password)) {
            return u;
        }
        return null;
    }
}
