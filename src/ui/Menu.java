package ui;

import model.*;
import enums.*;
import util.SystemConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Menu — semua tampilan UI dalam satu file.
 * Tidak menggunakan Collectors; semua iterasi pakai for-each manual.
 *
 * Fitur 3.2.2: showRegistration, registerRestaurant, registerShelter,
 *              showAdmin (verifikasi), showRestaurant/Shelter (login/logout/riwayat).
 * Fitur 3.2.5: adminViewAuditLog, adminViewUnmatched, adminViewActiveDonations,
 *              adminViewDashboard (alert via admin.viewDashboard()).
 */
public class Menu {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TM_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ═══════════════════════════════════════════════════════════
    //  REGISTRATION — Fitur 3.2.2
    // ═══════════════════════════════════════════════════════════

    public static void showRegistration(AppContext ctx, Scanner sc) {
        FoodSaverApp.printHeader("REGISTRASI AKUN BARU");
        System.out.println("  [1] Daftar sebagai Restoran");
        System.out.println("  [2] Daftar sebagai Panti");
        System.out.println("  [0] Kembali");
        FoodSaverApp.printDivider();
        String choice = FoodSaverApp.readMenuChoice(sc, "Pilihan: ", "1", "2", "0");
        switch (choice) {
            case "1" -> registerRestaurant(ctx, sc);
            case "2" -> registerShelter(ctx, sc);
        }
    }

    /**
     * Fitur 3.2.2 — Registrasi restoran.
     * Output: akun tersimpan ke userMap (HashMap) dengan status PENDING.
     * Dicatat ke AuditLog (ActionType.REGISTER).
     */
    private static void registerRestaurant(AppContext ctx, Scanner sc) {
        FoodSaverApp.printHeader("REGISTRASI RESTORAN");

        String name     = FoodSaverApp.readNonEmpty(sc, "Nama restoran     : ");
        String owner    = FoodSaverApp.readNonEmpty(sc, "Nama pemilik      : ");
        String address  = FoodSaverApp.readNonEmpty(sc, "Alamat            : ");
        double lat      = FoodSaverApp.readDoubleInRange(sc, "Koordinat (lat)   : ", -90.0, 90.0);
        double lon      = FoodSaverApp.readDoubleInRange(sc, "Koordinat (lon)   : ", -180.0, 180.0);
        String category = FoodSaverApp.readNonEmpty(sc, "Jenis makanan     : ");
        String phone    = FoodSaverApp.readPhoneNumber(sc, "No. HP            : ");

        String username;
        while (true) {
            username = FoodSaverApp.readUsername(sc, "Username          : ");
            if (ctx.userMap.containsKey(username))
                System.out.println("[✗] Username \"" + username + "\" sudah digunakan.");
            else break;
        }
        String password = FoodSaverApp.readPassword(sc, "Password          : ");

        Restaurant r = new Restaurant(name, owner, username, password, phone, address, lat, lon, category);
        ctx.userMap.put(r.getUsername(), r);
        ctx.auditLog.log(username, ActionType.REGISTER, r.getUserId(), "Registered restaurant: " + name);

        System.out.println("\n[✓] Registrasi berhasil. Akun Anda sedang menunggu verifikasi admin.");
    }

    /**
     * Fitur 3.2.2 — Registrasi panti.
     * Output: akun tersimpan ke userMap dan ShelterRegistry (HashMap) dengan status PENDING.
     * Dicatat ke AuditLog (ActionType.REGISTER).
     */
    private static void registerShelter(AppContext ctx, Scanner sc) {
        FoodSaverApp.printHeader("REGISTRASI PANTI");

        String name    = FoodSaverApp.readNonEmpty(sc, "Nama panti        : ");
        String manager = FoodSaverApp.readNonEmpty(sc, "Nama pengurus     : ");
        String address = FoodSaverApp.readNonEmpty(sc, "Alamat            : ");
        double lat     = FoodSaverApp.readDoubleInRange(sc, "Koordinat (lat)   : ", -90.0, 90.0);
        double lon     = FoodSaverApp.readDoubleInRange(sc, "Koordinat (lon)   : ", -180.0, 180.0);
        int residents  = FoodSaverApp.readPositiveInt(sc, "Jumlah penghuni   : ");

        System.out.println("Jenis panti       : [1] Anak Yatim  [2] Lansia  [3] Disabilitas");
        int typeChoice = FoodSaverApp.readIntInRange(sc, "Pilihan           : ", 1, 3);
        ShelterType type = switch (typeChoice) {
            case 2 -> ShelterType.LANSIA;
            case 3 -> ShelterType.DISABILITAS;
            default -> ShelterType.ANAK_YATIM;
        };

        String phone = FoodSaverApp.readPhoneNumber(sc, "No. HP            : ");

        String username;
        while (true) {
            username = FoodSaverApp.readUsername(sc, "Username          : ");
            if (ctx.userMap.containsKey(username))
                System.out.println("[✗] Username \"" + username + "\" sudah digunakan.");
            else break;
        }
        String password = FoodSaverApp.readPassword(sc, "Password          : ");

        Shelter s = new Shelter(name, manager, username, password, phone, address, lat, lon, residents, type);
        ctx.userMap.put(s.getUsername(), s);
        ctx.registry.register(s);
        ctx.auditLog.log(username, ActionType.REGISTER, s.getUserId(), "Registered shelter: " + name);

        System.out.println("\n[✓] Registrasi berhasil. Akun Anda sedang menunggu verifikasi admin.");
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN MENU — Fitur 3.2.2 + 3.2.5
    // ═══════════════════════════════════════════════════════════

    public static void showAdmin(AppContext ctx, Scanner sc, Admin admin) {
        boolean running = true;
        while (running) {
            admin.viewDashboard(); // Fitur 3.2.5 — tampilkan alert aktif
            FoodSaverApp.printHeader("MENU ADMIN — " + admin.getUsername());
            System.out.println("  [1] Lihat & verifikasi akun pending");
            System.out.println("  [2] Donasi tidak tersalurkan");
            System.out.println("  [3] Stok donasi aktif");
            System.out.println("  [4] AuditLog");
            System.out.println("  [5] Cari panti by ID");
            System.out.println("  [0] Logout");
            FoodSaverApp.printDivider();
            String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ", "1","2","3","4","5","0");
            switch (ch) {
                case "1" -> adminVerifyAccounts(ctx, sc, admin);
                case "2" -> adminViewUnmatched(admin);
                case "3" -> adminViewActiveDonations(admin);
                case "4" -> adminViewAuditLog(ctx, sc);
                case "5" -> adminSearchShelter(sc, admin);
                case "0" -> { admin.logout(); running = false; }
            }
        }
    }

    /**
     * Fitur 3.2.2 — Admin verifikasi akun PENDING.
     * Proses: tampil daftar PENDING → pilih → APPROVED/REJECTED/SKIP → catat AuditLog.
     */
    private static void adminVerifyAccounts(AppContext ctx, Scanner sc, Admin admin) {
        List<User> pending = admin.viewPendingAccounts();
        if (pending.isEmpty()) {
            System.out.println("[!] Tidak ada akun yang menunggu verifikasi.");
            return;
        }

        FoodSaverApp.printHeader("DAFTAR AKUN MENUNGGU VERIFIKASI");
        for (int i = 0; i < pending.size(); i++) {
            User u = pending.get(i);
            String type = u instanceof Restaurant ? "RESTORAN" : "PANTI";
            System.out.printf("[%d] %s - %s%n", i + 1, type, u.getUsername());
            if (u instanceof Restaurant r)
                System.out.printf("    Pemilik: %s | HP: %s | Daftar: %s%n    Alamat: %s%n    Lat/Lon: %.4f, %.4f | Kategori: %s%n",
                        r.getOwnerName(), r.getPhone(), r.getRegisteredAt().format(DT_FMT),
                        r.getAddress(), r.getLat(), r.getLon(), r.getFoodCategory());
            else if (u instanceof Shelter s)
                System.out.printf("    Pengurus: %s | HP: %s | Daftar: %s%n    Alamat: %s%n    Penghuni: %d | Tipe: %s%n",
                        s.getManagerName(), s.getPhone(), s.getRegisteredAt().format(DT_FMT),
                        s.getAddress(), s.getResidents(), s.getShelterType());
            System.out.println();
        }

        int choice = FoodSaverApp.readIndexOrCancel(sc, "Pilih nomor akun (0=kembali): ", pending.size());
        if (choice == 0) return;

        User target = pending.get(choice - 1);
        System.out.println("\n=== VERIFIKASI AKUN ===");
        System.out.println("Panduan: Cek di Google Maps, konfirmasi alamat, hubungi HP jika perlu.");
        String decision = FoodSaverApp.readMenuChoice(sc, "Keputusan [APPROVED/REJECTED/SKIP]: ",
                "APPROVED", "REJECTED", "SKIP");
        if (decision.equals("SKIP")) return;

        System.out.print("Catatan (opsional): ");
        String notes = sc.nextLine();
        admin.verifyAccount(target, decision, notes);
    }

    /**
     * Fitur 3.2.5 — Tampilkan donasi WAITING yang belum match + sisa waktu.
     */
    private static void adminViewUnmatched(Admin admin) {
        List<FoodDonation> unmatched = admin.viewUnmatchedDonations();
        if (unmatched.isEmpty()) {
            System.out.println("[✓] Semua donasi sudah dicocokkan atau antrian kosong.");
            return;
        }
        FoodSaverApp.printHeader("DONASI TIDAK TERSALURKAN");
        for (FoodDonation d : unmatched)
            System.out.printf("  %s | %-20s | %3d porsi | sisa %d mnt | dari: %s%n",
                    d.getDonationId(), d.getFoodName(), d.getPortions(),
                    d.getRemainingMinutes(), d.getRestaurant().getName());
    }

    /**
     * Fitur 3.2.5 — Stok donasi aktif di antrian beserta sisa waktu expiry.
     */
    private static void adminViewActiveDonations(Admin admin) {
        List<FoodDonation> active = admin.viewActiveDonations();
        if (active.isEmpty()) {
            System.out.println("[!] Antrian donasi kosong.");
            return;
        }
        FoodSaverApp.printHeader("STOK DONASI AKTIF");
        for (FoodDonation d : active)
            System.out.printf("  %s | %-20s | %3d porsi | expired: %s | sisa: %d mnt%n",
                    d.getDonationId(), d.getFoodName(), d.getPortions(),
                    d.getExpiredAt().format(TM_FMT), d.getRemainingMinutes());
    }

    /**
     * Fitur 3.2.5 — AuditLog: semua entri, filter by donation ID, atau hanya EXPIRED+WASTED.
     */
    private static void adminViewAuditLog(AppContext ctx, Scanner sc) {
        FoodSaverApp.printHeader("AUDIT LOG");
        System.out.println("  [1] Semua entri");
        System.out.println("  [2] Filter berdasarkan donation ID");
        System.out.println("  [3] Hanya EXPIRED dan WASTED");
        String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ", "1", "2", "3");
        List<AuditEntry> entries = switch (ch) {
            case "2" -> {
                String id = FoodSaverApp.readNonEmpty(sc, "Masukkan donation ID: ");
                yield ctx.auditLog.filterByDonationId(id);
            }
            case "3" -> ctx.auditLog.filterExpiredAndWasted();
            default  -> ctx.auditLog.getAll();
        };
        if (entries.isEmpty()) { System.out.println("[!] Tidak ada entri."); return; }
        entries.forEach(System.out::println);
    }

    /**
     * Fitur 3.2.2 — Pencarian panti by ID menggunakan HashMap O(1).
     */
    private static void adminSearchShelter(Scanner sc, Admin admin) {
        String id = FoodSaverApp.readNonEmpty(sc, "Masukkan Shelter ID: ");
        Shelter s = admin.searchShelterById(id);
        if (s == null) { System.out.println("[✗] Panti tidak ditemukan."); return; }
        System.out.printf("[✓] %s | Penghuni: %d | Kebutuhan: %d | Status: %s%n",
                s.getName(), s.getResidents(), s.getRemainingNeed(), s.getAccountStatus());
    }

    // ═══════════════════════════════════════════════════════════
    //  RESTAURANT MENU — Fitur 3.2.2
    // ═══════════════════════════════════════════════════════════

    public static void showRestaurant(AppContext ctx, Scanner sc, Restaurant restaurant) {
        boolean running = true;
        while (running) {
            FoodSaverApp.printHeader("MENU RESTORAN — " + restaurant.getName());
            System.out.println("  [1] Posting donasi makanan");
            System.out.println("  [2] Riwayat donasi saya");
            System.out.println("  [3] Edit porsi donasi");
            System.out.println("  [4] Batalkan donasi");
            System.out.println("  [0] Logout");
            FoodSaverApp.printDivider();
            String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ", "1", "2", "3", "4", "0");
            switch (ch) {
                case "1" -> restaurantPostDonation(ctx, sc, restaurant);
                case "2" -> restaurantViewHistory(restaurant);
                case "3" -> restaurantEditPortions(sc, restaurant);
                case "4" -> restaurantCancelDonation(ctx, sc, restaurant);
                case "0" -> { restaurant.logout(); running = false; }
            }
        }
    }

    private static void restaurantPostDonation(AppContext ctx, Scanner sc, Restaurant restaurant) {
        FoodSaverApp.printHeader("POSTING DONASI BARU");

        String foodName  = FoodSaverApp.readNonEmpty(sc, "Jenis makanan           : ");
        int portions     = FoodSaverApp.readPositiveInt(sc, "Jumlah porsi            : ");
        String timeStr   = FoodSaverApp.readTime(sc, "Waktu masak (HH:MM)     : ");
        String[] parts   = timeStr.split(":");
        LocalDateTime cookedAt = LocalDateTime.now()
                .withHour(Integer.parseInt(parts[0])).withMinute(Integer.parseInt(parts[1]))
                .withSecond(0).withNano(0);
        if (cookedAt.isAfter(LocalDateTime.now())) cookedAt = cookedAt.minusDays(1);

        System.out.print("Catatan tambahan        : ");
        String notes = sc.nextLine();

        LocalDateTime now = LocalDateTime.now();
        long minutesSinceCooked = java.time.temporal.ChronoUnit.MINUTES.between(cookedAt, now);
        long maxMinutes = SystemConfig.MAX_FRESH_HOURS * 60L;

        System.out.println("\n  [✓] Waktu sekarang  : " + now.format(TM_FMT));
        System.out.println("  [✓] Waktu masak     : " + cookedAt.format(TM_FMT));
        System.out.printf("  [✓] Selisih         : %d menit (batas %d jam)%n", minutesSinceCooked, SystemConfig.MAX_FRESH_HOURS);

        if (minutesSinceCooked > maxMinutes) {
            System.out.println("\n  [✗] Donasi DITOLAK — makanan sudah melebihi batas kesegaran.");
            return;
        }
        System.out.println("  [✓] Donasi valid — masih segar.");

        FoodDonation donation = new FoodDonation(foodName, portions, cookedAt, notes, restaurant);
        restaurant.postDonation(donation);
        ctx.submitDonation(donation);

        System.out.println("\n  ID Donasi : " + donation.getDonationId());
        System.out.println("  Expired   : " + donation.getExpiredAt().format(TM_FMT));
    }

    /**
     * Fitur 3.2.2 — Riwayat donasi restoran (LinkedList dari Restaurant.donations).
     */
    private static void restaurantViewHistory(Restaurant restaurant) {
        List<FoodDonation> history = restaurant.viewDonationHistory();
        if (history.isEmpty()) { System.out.println("[!] Belum ada riwayat donasi."); return; }
        FoodSaverApp.printHeader("RIWAYAT DONASI — " + restaurant.getName());
        for (FoodDonation d : history)
            System.out.printf("  %s | %-20s | %3d porsi | status: %s%n",
                    d.getDonationId(), d.getFoodName(), d.getPortions(), d.getStatus());
    }

    private static void restaurantEditPortions(Scanner sc, Restaurant restaurant) {
        String id       = FoodSaverApp.readNonEmpty(sc, "Masukkan Donation ID: ");
        int newPortions = FoodSaverApp.readPositiveInt(sc, "Jumlah porsi baru   : ");
        restaurant.editPortions(id, newPortions);
    }

    private static void restaurantCancelDonation(AppContext ctx, Scanner sc, Restaurant restaurant) {
        String id = FoodSaverApp.readNonEmpty(sc, "Masukkan Donation ID yang akan dibatalkan: ");
        restaurant.cancelDonation(id);
        // Hapus dari pool dan expiryTree jika ada
        List<FoodDonation> poolList = ctx.pool.getAll();
        for (FoodDonation d : poolList) {
            if (d.getDonationId().equals(id)) {
                ctx.pool.remove(d);
                ctx.expiryTree.remove(d);
                break;
            }
        }
        System.out.println("  [✓] Donasi valid — masih segar.");

        FoodDonation donation = new FoodDonation(foodName, portions, cookedAt, notes, restaurant);
        restaurant.postDonation(donation);
        ctx.submitDonation(donation);

        System.out.println("\n  ID Donasi : " + donation.getDonationId());
        System.out.println("  Expired   : " + donation.getExpiredAt().format(TM_FMT));
    }

    // ═══════════════════════════════════════════════════════════
    //  SHELTER MENU — Fitur 3.2.2
    // ═══════════════════════════════════════════════════════════

    public static void showShelter(AppContext ctx, Scanner sc, Shelter shelter) {
        boolean running = true;
        while (running) {
            long pending = getShelterIncomingOrders(ctx, shelter).size();
            FoodSaverApp.printHeader("MENU PANTI — " + shelter.getName());
            System.out.printf("  [1] Konfirmasi donasi masuk%s%n",
                    pending > 0 ? "   [ADA " + pending + " MENUNGGU]" : "");
            System.out.println("  [2] Riwayat penerimaan");
            System.out.println("  [3] Perbarui jumlah penghuni");
            System.out.println("  [0] Logout");
            FoodSaverApp.printDivider();
            String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ", "1", "2", "3", "0");
            switch (ch) {
                case "1" -> shelterConfirmDonation(ctx, sc, shelter);
                case "2" -> shelterViewHistory(ctx, shelter);
                case "3" -> shelterUpdateResidents(sc, shelter);
                case "0" -> { shelter.logout(); running = false; }
            }
        }
    }

    /** Ambil order IN_TRANSIT/WAITING_PICKUP/PICKED_UP untuk panti ini — tanpa Collectors */
    private static List<DeliveryOrder> getShelterIncomingOrders(AppContext ctx, Shelter shelter) {
        List<DeliveryOrder> result = new ArrayList<>();
        for (DeliveryOrder o : ctx.history.getAll()) {
            if (!o.getShelter().getUserId().equals(shelter.getUserId())) continue;
            if (o.getStatus() == OrderStatus.IN_TRANSIT
                    || o.getStatus() == OrderStatus.WAITING_PICKUP
                    || o.getStatus() == OrderStatus.PICKED_UP) {
                result.add(o);
            }
        }
        return result;
    }

    /**
     * Fitur 3.2.2 — Panti konfirmasi penerimaan, beri rating, catat AuditLog.
     * Output: status DELIVERED, riwayat tersimpan di LinkedList Shelter.
     */
    private static void shelterConfirmDonation(AppContext ctx, Scanner sc, Shelter shelter) {
        List<DeliveryOrder> incoming = getShelterIncomingOrders(ctx, shelter);
        if (incoming.isEmpty()) {
            System.out.println("[!] Tidak ada donasi yang menunggu konfirmasi.");
            return;
        }

        FoodSaverApp.printHeader("KONFIRMASI PENERIMAAN");
        for (int i = 0; i < incoming.size(); i++) {
            DeliveryOrder o = incoming.get(i);
            System.out.printf("[%d] Order: %s%n    Dari   : ", i + 1, o.getOrderId());
            for (Restaurant r : o.getBundle().getRestaurantList())
                System.out.print(r.getName() + " ");
            System.out.printf("%n    Porsi  : %d%n    Status : %s%n",
                    o.getBundle().getTotalPortions(), o.getStatus());
        }

        int choice = FoodSaverApp.readIndexOrCancel(sc, "Pilih nomor order (0=batal): ", incoming.size());
        if (choice == 0) return;

        DeliveryOrder order = incoming.get(choice - 1);
        if (!FoodSaverApp.readYesNo(sc, "\nKonfirmasi diterima? [Y/N]: ")) {
            System.out.println("[!] Konfirmasi dibatalkan.");
            return;
        }

        int    rating = FoodSaverApp.readIntInRange(sc, "Rating donasi (1–5)        : ", 1, 5);
        System.out.print("Catatan                    : ");
        String notes  = sc.nextLine();

        shelter.confirmReceipt(order, rating, notes);
        ctx.auditLog.log(shelter.getUsername(), ActionType.DELIVER,
                order.getOrderId(), "Confirmed by shelter. Rating: " + rating);
        System.out.println("[✓] Penerimaan dikonfirmasi. Status order: DELIVERED");
    }

    /**
     * Fitur 3.2.2 — Riwayat penerimaan panti beserta rating (LinkedList DeliveryHistory).
     */
    private static void shelterViewHistory(AppContext ctx, Shelter shelter) {
        List<DeliveryOrder> all = ctx.history.filterByShelter(shelter.getUserId());
        if (all.isEmpty()) {
            System.out.println("[!] Belum ada riwayat penerimaan.");
            return;
        }
        FoodSaverApp.printHeader("RIWAYAT PENERIMAAN — " + shelter.getName());
        for (DeliveryOrder o : all)
            System.out.printf("  %s | Porsi: %3d | Rating: %d | Status: %s%n",
                    o.getOrderId(), o.getBundle().getTotalPortions(), o.getRating(), o.getStatus());
    }

    private static void shelterUpdateResidents(Scanner sc, Shelter shelter) {
        int count = FoodSaverApp.readPositiveInt(sc, "Jumlah penghuni aktif saat ini: ");
        shelter.updateResidents(count);
    }
}
