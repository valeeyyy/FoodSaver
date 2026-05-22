package ui;

import datastructure.*;
import engine.MatchingEngine;
import model.*;
import enums.AccountStatus;
import enums.DonationStatus;

import java.util.ArrayList;
import java.util.List;

public class AppContext {

    public final DonationPool pool;
    public final ShelterRegistry registry;
    public final FoodExpiryTree expiryTree;
    public final DeliveryHistory history;
    public final AuditLog auditLog;
    public final List<User> allUsers;
    public final MatchingEngine engine;
    public final Admin admin;

    public AppContext() {
        pool = new DonationPool();
        registry = new ShelterRegistry();
        expiryTree = new FoodExpiryTree();
        history = new DeliveryHistory();
        auditLog = new AuditLog();
        allUsers = new ArrayList<>();

        engine = new MatchingEngine(pool, registry, expiryTree, history, auditLog);

        pool.injectDependencies(auditLog, engine);

        admin = new Admin("admin", "admin123", 1);
        admin.injectDependencies(pool, registry, history, auditLog, allUsers);
        allUsers.add(admin);
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
                auditLog.log("SYSTEM",
                        enums.ActionType.STARTUP_CLEANUP,
                        d.getDonationId(), "Moved to history as WASTED on startup");
            }
        }
        for (FoodDonation d : toClean) {
            pool.remove(d);
            expiryTree.remove(d);
        }

        System.out.println("[✓] Startup selesai. Akun admin default: admin / admin123");
        System.out.println();
    }

    public void submitDonation(FoodDonation d) {
        pool.enqueue(d);
        expiryTree.insert(d);
        auditLog.log(d.getRestaurant().getUsername(),
                enums.ActionType.POST,
                d.getDonationId(), "Posted: " + d.getFoodName());

        System.out.println("\n[MatchingEngine] Donasi baru masuk — menjalankan matching...");
        engine.run();
    }

    public User findUser(String username, String password) {
        return allUsers.stream()
                .filter(u -> u.getUsername().equals(username)
                        && u.getPassword().equals(password))
                .findFirst().orElse(null);
    }

    public DeliveryOrder findOrder(String orderId) {
        for (DeliveryOrder o : history.getAll()) {
            if (o.getOrderId().equals(orderId))
                return o;
        }
        return null;
    }
}
