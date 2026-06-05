package ui;

import datastructure.*;
import engine.MatchingEngine;
import enums.AccountStatus;
import enums.ActionType;
import enums.DonationStatus;
import enums.ShelterType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.*;
import util.SystemConfig;

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

        admin = new Admin(SystemConfig.ADMIN_USERNAME, SystemConfig.ADMIN_PASSWORD, pool, registry, history, auditLog, userMap);
        userMap.put(admin.getUsername(), admin);
    }

    void seedDummyData() {
        // ===== RESTAURANTS =====
        // Catatan: r[3] "Warung Tegal Pak Joko" sengaja ditempatkan >5km dari semua panti
        // tapi dalam 8km dari Panti Lansia — sehingga donasi d3 memicu YELLOW ALERT.
        Restaurant[] r = {
            new Restaurant("Warung Nasi Berkah", "Budi Santoso",
                "warung_berkah", "berkah123",
                "081234567001", "Jl. Ahmad Yani No. 12, Surabaya",
                -7.2575, 112.7521, "Nasi & Lauk Pauk"),
            new Restaurant("Rumah Makan Ibu Sari", "Sari Dewi",
                "rm_ibusari", "ibusari123",
                "081234567002", "Jl. Diponegoro No. 45, Surabaya",
                -7.2650, 112.7380, "Masakan Rumahan"),
            new Restaurant("Resto Padang Minang", "Andi Putra",
                "padang_minang", "minang123",
                "081234567003", "Jl. Pemuda No. 88, Surabaya",
                -7.2490, 112.7410, "Masakan Padang"),
            new Restaurant("Warung Tegal Pak Joko", "Joko Santoso",
                "warung_pakjoko", "pakjoko123",
                "081234567004", "Jl. Raya Benowo No. 15, Surabaya",
                -7.3000, 112.6560, "Masakan Jawa & Tegal"),
            new Restaurant("Catering Nusantara", "Rina Wahyuni",
                "catering_nusa", "nusa123",
                "081234567005", "Jl. Gubeng Jaya No. 7, Surabaya",
                -7.2680, 112.7520, "Catering & Nasi Box")
        };
        for (Restaurant res : r) {
            res.setAccountStatus(AccountStatus.APPROVED);
            userMap.put(res.getUsername(), res);
            auditLog.log("admin", ActionType.APPROVE, res.getUserId(), "Restaurant approved: " + res.getName());
        }

        // ===== SHELTERS (5 APPROVED) =====
        Object[][] sd = {
            {"Panti Asuhan Al-Ikhlas",   "Hj. Fatimah",      "panti_alikhlas",    "alikhlas123",
             "081234568001", "Jl. Kenjeran No. 25, Surabaya",         -7.2440, 112.7730, 80, ShelterType.ANAK_YATIM},
            {"Panti Lansia Sejahtera",   "Bambang Susilo",    "panti_lansia",       "lansia123",
             "081234568002", "Jl. Mayjen Sungkono No. 10, Surabaya",  -7.2920, 112.7220, 50, ShelterType.LANSIA},
            {"Rumah Singgah Harapan",    "Dewi Kurniasih",    "panti_harapan",      "harapan123",
             "081234568003", "Jl. Kertajaya No. 56, Surabaya",        -7.2750, 112.7630, 40, ShelterType.ANAK_YATIM},
            {"Panti Disabilitas Kasih",  "Rudi Hermawan",     "panti_kasih",        "kasih123",
             "081234568004", "Jl. Ngagel No. 33, Surabaya",           -7.2870, 112.7490, 35, ShelterType.DISABILITAS},
            {"Panti Asuhan Baitussalam", "Ustadz Zainal",     "panti_baitussalam",  "baitussalam123",
             "081234568005", "Jl. Wonokromo No. 18, Surabaya",        -7.3010, 112.7350, 65, ShelterType.ANAK_YATIM}
        };
        Shelter[] s = new Shelter[sd.length];
        for (int i = 0; i < sd.length; i++) {
            s[i] = new Shelter((String)sd[i][0], (String)sd[i][1], (String)sd[i][2], (String)sd[i][3],
                    (String)sd[i][4], (String)sd[i][5], (double)sd[i][6], (double)sd[i][7],
                    (int)sd[i][8], (ShelterType)sd[i][9]);
            s[i].setAccountStatus(AccountStatus.APPROVED);
            // Panti demo menerima donasi 24 jam — sesuai siklus donasi malam hari
            // (proposal §1.2.2) sehingga matching tetap berjalan kapan pun program dijalankan.
            s[i].setReceptionStartHour(0);
            s[i].setReceptionEndHour(24);
            userMap.put(s[i].getUsername(), s[i]);
            registry.register(s[i]);
            auditLog.log("admin", ActionType.APPROVE, s[i].getUserId(), "Shelter approved: " + s[i].getName());
        }

        // ===== PENDING SHELTER — demo verifikasi admin =====
        Shelter pendingShelter = new Shelter("Panti Sosial Baru Mandiri", "Ahmad Fauzi",
                "panti_mandiri", "mandiri123", "081234569001",
                "Jl. Rungkut No. 55, Surabaya", -7.3100, 112.7600, 30, ShelterType.ANAK_YATIM);
        userMap.put(pendingShelter.getUsername(), pendingShelter);
        auditLog.log("panti_mandiri", ActionType.REGISTER, pendingShelter.getUserId(),
                "Registered: " + pendingShelter.getName() + " (PENDING)");

        System.out.println("[✓] 5 restoran + 5 panti (APPROVED) + 1 panti PENDING (perlu verifikasi).");
        System.out.println();

        // ===== DONATIONS — engine berjalan normal, alert terpicu sesuai kondisi =====

        // d1: Nasi Ayam, 90p, ~4 jam — cocok normal 5km
        FoodDonation d1 = new FoodDonation("Nasi Putih + Ayam Goreng", 90,
                LocalDateTime.now().minusHours(2), "Masih hangat, baru dimasak siang", r[0]);
        r[0].postDonation(d1);
        submitDonation(d1);

        // d2: Rendang + Nasi, 50p, ~3 jam — cocok normal 5km
        FoodDonation d2 = new FoodDonation("Rendang + Nasi", 50,
                LocalDateTime.now().minusHours(3), "Rendang spesial", r[2]);
        r[2].postDonation(d2);
        submitDonation(d2);

        // d3: Mie Ayam, 40p, ~1.5 jam — r[3] jauh dari semua panti (>5km),
        //     hanya Panti Lansia dalam 8km → YELLOW ALERT terpicu, cocok via radius diperluas
        FoodDonation d3 = new FoodDonation("Mie Ayam Spesial", 40,
                LocalDateTime.now().minusHours(4).minusMinutes(30),
                "Hampir habis masa segar", r[3]);
        r[3].postDonation(d3);
        submitDonation(d3);

        // d4: Nasi Box, 65p, ~1 jam — cocok normal 5km
        FoodDonation d4 = new FoodDonation("Nasi Box Catering", 65,
                LocalDateTime.now().minusHours(5),
                "Sisa catering acara kantor", r[4]);
        r[4].postDonation(d4);
        submitDonation(d4);

        // d5: Bakso Kuah, 30p, ~20 mnt — masuk buffer 30 mnt → RED ALERT → EXPIRED
        FoodDonation d5 = new FoodDonation("Bakso Kuah", 30,
                LocalDateTime.now().minusHours(5).minusMinutes(40),
                "Segera diambil sebelum expired!", r[1]);
        r[1].postDonation(d5);
        submitDonation(d5);

        // ===== RINGKASAN =====
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│               [✓] DUMMY DATA BERHASIL DIMUAT                │");
        System.out.println("├──────────────────────┬──────────────────┬───────────────────┤");
        System.out.printf( "│ %-21s│ %-17s│ %-18s│%n", "Makanan", "Alert", "Sisa Waktu");
        System.out.println("├──────────────────────┼──────────────────┼───────────────────┤");
        System.out.printf( "│ %-21s│ %-17s│ %-18s│%n", "Nasi Ayam",   "NORMAL",       "~4 jam");
        System.out.printf( "│ %-21s│ %-17s│ %-18s│%n", "Rendang",     "NORMAL",       "~3 jam");
        System.out.printf( "│ %-21s│ %-17s│ %-18s│%n", "Mie Ayam",    "YELLOW ALERT", "~1.5 jam");
        System.out.printf( "│ %-21s│ %-17s│ %-18s│%n", "Nasi Box",    "NORMAL",       "~1 jam");
        System.out.printf( "│ %-21s│ %-17s│ %-18s│%n", "Bakso Kuah",  "RED ALERT",    "~20 menit");
        System.out.println("└──────────────────────┴──────────────────┴───────────────────┘");
        System.out.printf("  ⚠ Panti PENDING menunggu verifikasi: %s (login: %s / mandiri123)%n",
                pendingShelter.getName(), pendingShelter.getUsername());
        System.out.println();
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

        System.out.println("[✓] Startup selesai. Login sebagai: " + SystemConfig.ADMIN_USERNAME + "\n");
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
        if (u != null && u.login(username, password)) {
            return u;
        }
        return null;
    }
}