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

/**
 * AppContext — pusat data dan dependensi seluruh sistem.
 *
 * Fitur 3.2.2:
 *   - userMap (HashMap) untuk login O(1) dan cek username duplikat
 *   - findUser() untuk autentikasi
 * Fitur 3.2.5:
 *   - startup() membersihkan donasi EXPIRED_UNDELIVERED → WASTED ke history
 *   - submitDonation() menjalankan MatchingEngine dan mencatat ke AuditLog
 */
public class AppContext {

    public final DonationPool    pool;
    public final ShelterRegistry registry;
    public final FoodExpiryTree  expiryTree;
    public final DeliveryHistory history;
    public final AuditLog        auditLog;

    /**
     * Fitur 3.2.2 — HashMap<username, User> untuk pencarian akun O(1) saat login.
     * Kunci = username (unik), value = objek User (Admin/Restaurant/Shelter).
     */
    public final Map<String, User> userMap;

    public final MatchingEngine  engine;
    public final Admin           admin;

    public AppContext() {
        registry   = new ShelterRegistry();
        expiryTree = new FoodExpiryTree();
        history    = new DeliveryHistory();
        auditLog   = new AuditLog();
        userMap    = new HashMap<>();

        pool   = new DonationPool(auditLog);
        engine = new MatchingEngine(pool, registry, expiryTree, history, auditLog);
        pool.setEngine(engine);

        // Admin constructor disatukan — tidak ada injectDependencies terpisah
        admin = new Admin("admin", "admin123", pool, registry, history, auditLog, userMap);
        userMap.put(admin.getUsername(), admin);
    }

    /**
     * Fitur 3.2.5 — Startup: reset porsi harian panti, bersihkan donasi EXPIRED_UNDELIVERED.
     * Proses: EXPIRED_UNDELIVERED → markAsWasted → pindah ke DeliveryHistory sebagai WASTED.
     * Semua dicatat ke AuditLog dengan ActionType.STARTUP_CLEANUP.
     */
    public void startup() {
        System.out.println("=== FOODSAVER STARTUP ===");

        // Reset porsi harian semua panti
        for (Shelter s : registry.getAll()) {
            s.resetDailyPortions();
        }

        // Pembersihan donasi EXPIRED_UNDELIVERED dari sesi sebelumnya
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

    /**
     * Fitur 3.2.5 — Submit donasi baru: masuk Queue, masuk TreeMap expiry, catat AuditLog,
     * lalu jalankan MatchingEngine otomatis.
     */
    public void submitDonation(FoodDonation d) {
        pool.enqueue(d);
        expiryTree.insert(d);
        auditLog.log(d.getRestaurant().getUsername(), ActionType.POST,
                d.getDonationId(), "Posted: " + d.getFoodName());
        System.out.println("\n[MatchingEngine] Donasi baru masuk — menjalankan matching...");
        engine.run();
    }

    /**
     * Fitur 3.2.2 — Autentikasi login via HashMap (O(1)).
     * Mengembalikan User jika username + password cocok, null jika tidak.
     */
    public User findUser(String username, String password) {
        User u = userMap.get(username);
        if (u != null && u.getPassword().equals(password)) {
            return u;
        }
        return null;
    }
}
