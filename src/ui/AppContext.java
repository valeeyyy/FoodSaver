package ui;

import java.util.*;

import datastructure.AuditLog;
import datastructure.DeliveryHistory;
import datastructure.DonationPool;
import datastructure.FoodExpiryTree;
import datastructure.ShelterRegistry;
import engine.MatchingEngine;
import enums.ActionType;
import enums.DonationStatus;
import model.Admin;
import model.FoodDonation;
import model.Shelter;
import model.User;

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
        registry = new ShelterRegistry();
        auditLog = new AuditLog();
        history = new DeliveryHistory();
        expiryTree = new FoodExpiryTree();
        allUsers = new ArrayList<>();

        pool = new DonationPool(auditLog);

        engine = new MatchingEngine();

        pool.setEngine(engine);

        admin = new Admin("admin", "admin123", registry, auditLog, allUsers);
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
                auditLog.log("SYSTEM", ActionType.STARTUP_CLEANUP,
                        d.getDonationId(), "Moved to history as WASTED on startup");
            }
        }
        for (FoodDonation d : toClean) {
            pool.remove(d);
            expiryTree.remove(d);
            history.addFirst(d);

            System.out.println("[✓] Startup selesai. Akun admin default: admin / admin123");
            System.out.println();
        }
    }
}