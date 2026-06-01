package ui;

import datastructure.*;
import engine.MatchingEngine;
import model.*;
import enums.AccountStatus;
import enums.ActionType;
import enums.DonationStatus;
import enums.ShelterType;

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
        pool.setHistory(history);

        admin = new Admin("admin", "admin123", pool, registry, history, auditLog, userMap);
        userMap.put(admin.getUsername(), admin);

        seedDummyData();
    }

    private void seedDummyData() {
        Restaurant[] restaurants = {
            new Restaurant("Warung Nasi Berkah",    "Budi Santoso",
                           "warung_berkah",  "berkah123",
                           "081234567001", "Jl. Ahmad Yani No. 12, Surabaya",
                           -7.2575, 112.7521, "Nasi & Lauk Pauk"),

            new Restaurant("Rumah Makan Ibu Sari",  "Sari Dewi",
                           "rm_ibusari",     "ibusari123",
                           "081234567002", "Jl. Diponegoro No. 45, Surabaya",
                           -7.2650, 112.7380, "Masakan Rumahan"),

            new Restaurant("Resto Padang Minang",   "Andi Putra",
                           "padang_minang",  "minang123",
                           "081234567003", "Jl. Pemuda No. 88, Surabaya",
                           -7.2490, 112.7410, "Masakan Padang"),

            new Restaurant("Kedai Bakso Pak Joko",  "Joko Widodo",
                           "bakso_pakjoko",  "bakso123",
                           "081234567004", "Jl. Raya Darmo No. 30, Surabaya",
                           -7.2830, 112.7340, "Bakso & Mie"),

            new Restaurant("Catering Nusantara",    "Rina Wahyuni",
                           "catering_nusa",  "nusa123",
                           "081234567005", "Jl. Gubeng Jaya No. 7, Surabaya",
                           -7.2680, 112.7520, "Catering & Nasi Box")
        };

        for (Restaurant r : restaurants) {
            r.setAccountStatus(AccountStatus.APPROVED);
            userMap.put(r.getUsername(), r);
            auditLog.log("admin", ActionType.APPROVE, r.getUserId(),
                         "[DUMMY] Restaurant approved: " + r.getName());
        }

        Object[][] shelterData = {
            { "Panti Asuhan Al-Ikhlas",      "Hj. Fatimah",
              "panti_alikhlas",  "alikhlas123",
              "081234568001", "Jl. Kenjeran No. 25, Surabaya",
              -7.2440, 112.7730, 80, ShelterType.ANAK_YATIM },

            { "Panti Lansia Sejahtera",      "Bambang Susilo",
              "panti_lansia",    "lansia123",
              "081234568002", "Jl. Mayjen Sungkono No. 10, Surabaya",
              -7.2920, 112.7220, 50, ShelterType.LANSIA },

            { "Rumah Singgah Harapan",       "Dewi Kurniasih",
              "panti_harapan",   "harapan123",
              "081234568003", "Jl. Kertajaya No. 56, Surabaya",
              -7.2750, 112.7630, 40, ShelterType.ANAK_YATIM },

            { "Panti Disabilitas Kasih",     "Rudi Hermawan",
              "panti_kasih",     "kasih123",
              "081234568004", "Jl. Ngagel No. 33, Surabaya",
              -7.2870, 112.7490, 35, ShelterType.DISABILITAS },

            { "Panti Asuhan Baitussalam",    "Ustadz Zainal",
              "panti_baitussalam", "baitussalam123",
              "081234568005", "Jl. Wonokromo No. 18, Surabaya",
              -7.3010, 112.7350, 65, ShelterType.ANAK_YATIM }
        };

        for (Object[] data : shelterData) {
            Shelter s = new Shelter(
                (String)  data[0],
                (String)  data[1],
                (String)  data[2],
                (String)  data[3],
                (String)  data[4],
                (String)  data[5],
                (double)  data[6],
                (double)  data[7],
                (int)     data[8],
                (ShelterType) data[9]
            );
            s.setAccountStatus(AccountStatus.APPROVED);
            userMap.put(s.getUsername(), s);
            registry.register(s);
            auditLog.log("admin", ActionType.APPROVE, s.getUserId(),
                         "[DUMMY] Shelter approved: " + s.getName());
        }

        System.out.println("[✓] Dummy data loaded: 5 restoran + 5 panti (semua APPROVED).");
    }

    public void startup() {
        System.out.println("=== FOODSAVER STARTUP ===");

        for (Shelter s : registry.getAll()) {
            s.resetDailyPortions();
        }

        List<FoodDonation> toClean = new ArrayList<>();
        for (FoodDonation d : pool.getAll()) {
            if (d.getStatus() == DonationStatus.EXPIRED_UNDELIVERED
                    || (d.getStatus() == DonationStatus.WAITING && !d.isStillFresh())) {
                d.markAsWasted();
                toClean.add(d);
                Notification notif = Notification.createStartupCleanup(d.getDonationId());
                notif.display();
                auditLog.log(notif.toAuditEntry());
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
        pool.checkAlerts();
    }

    public User findUser(String username, String password) {
        User u = userMap.get(username);
        if (u != null && u.getPassword().equals(password)) {
            return u;
        }
        return null;
    }
}