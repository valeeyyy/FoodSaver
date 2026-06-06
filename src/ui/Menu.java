package ui;

import enums.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import model.*;
import util.GeoUtils;
import util.SystemConfig;

public class Menu {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TM_FMT = DateTimeFormatter.ofPattern("HH:mm");

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

    private static void registerRestaurant(AppContext ctx, Scanner sc) {
        FoodSaverApp.printHeader("REGISTRASI RESTORAN");

        String name = FoodSaverApp.readNonEmpty(sc, "Nama restoran     : ");
        String owner = FoodSaverApp.readNonEmpty(sc, "Nama pemilik      : ");
        String address = FoodSaverApp.readNonEmpty(sc, "Alamat            : ");
        double lat = FoodSaverApp.readDoubleInRange(sc, "Koordinat (lat)   : ", -90.0, 90.0);
        double lon = FoodSaverApp.readDoubleInRange(sc, "Koordinat (lon)   : ", -180.0, 180.0);
        String category = FoodSaverApp.readNonEmpty(sc, "Jenis makanan     : ");
        String phone = FoodSaverApp.readPhoneNumber(sc, "No. HP            : ");

        String username;
        while (true) {
            username = FoodSaverApp.readUsername(sc, "Username          : ");
            if (ctx.userMap.containsKey(username))
                System.out.println("[✗] Username \"" + username + "\" sudah digunakan.");
            else
                break;
        }
        String password = FoodSaverApp.readPassword(sc, "Password          : ");

        Restaurant r = new Restaurant(name, owner, username, password, phone, address, lat, lon, category);
        ctx.userMap.put(r.getUsername(), r);
        ctx.auditLog.log(username, ActionType.REGISTER, r.getUserId(), "Registered restaurant: " + name);

        System.out.println("\n[✓] Registrasi berhasil. Akun Anda sedang menunggu verifikasi admin.");
    }

    private static void registerShelter(AppContext ctx, Scanner sc) {
        FoodSaverApp.printHeader("REGISTRASI PANTI");

        String name = FoodSaverApp.readNonEmpty(sc, "Nama panti        : ");
        String manager = FoodSaverApp.readNonEmpty(sc, "Nama pengurus     : ");
        String address = FoodSaverApp.readNonEmpty(sc, "Alamat            : ");
        double lat = FoodSaverApp.readDoubleInRange(sc, "Koordinat (lat)   : ", -90.0, 90.0);
        double lon = FoodSaverApp.readDoubleInRange(sc, "Koordinat (lon)   : ", -180.0, 180.0);
        int residents = FoodSaverApp.readPositiveInt(sc, "Jumlah penghuni   : ");
        System.out.println("Jam penerimaan    : (format jam, contoh: 7 untuk 07:00)");
        int startHour = FoodSaverApp.readIntInRange(sc, "Mulai jam         : ", 0, 23);
        int endHour = FoodSaverApp.readIntInRange(sc, "Sampai jam        : ", startHour + 1, 24);

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
            else
                break;
        }
        String password = FoodSaverApp.readPassword(sc, "Password          : ");

        Shelter s = new Shelter(name, manager, username, password, phone, address, lat, lon, residents, type);
        s.setReceptionStartHour(startHour);
        s.setReceptionEndHour(endHour);
        ctx.userMap.put(s.getUsername(), s);
        ctx.registry.register(s);
        ctx.auditLog.log(username, ActionType.REGISTER, s.getUserId(), "Registered shelter: " + name);

        System.out.println("\n[✓] Registrasi berhasil. Akun Anda sedang menunggu verifikasi admin.");
    }

    public static void showAdmin(AppContext ctx, Scanner sc, Admin admin) {
        boolean running = true;
        while (running) {
            FoodSaverApp.printHeader("MENU ADMIN — " + admin.getUsername());
            System.out.println("  [0] Dashboard Ringkasan");
            System.out.println("  [1] Lihat & verifikasi akun pending");
            System.out.println("  [2] Donasi tidak tersalurkan");
            System.out.println("  [3] Stok donasi aktif");
            System.out.println("  [4] AuditLog");
            System.out.println("  [5] Cari panti by ID");
            System.out.println("  [6] Cari restoran / donasi by nama");
            System.out.println("  [7] Filter order by status");
            System.out.println("  [8] Cari AuditLog by username");
            System.out.println("  [9] Filter donasi by status (EXPIRED/WASTED)");
            System.out.println("  [10] Review permintaan edit profil");
            System.out.println("  [11] Update status pengiriman (kurir)");
            System.out.println("  [12] Lihat semua panti & restoran terdaftar");
            System.out.println("  [13] Lihat status alert aktif");
            System.out.println("  [L] Logout");
            FoodSaverApp.printDivider();
            String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ",
                    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "L");
            switch (ch) {
                case "0" -> adminViewDashboard(ctx, admin);
                case "1" -> adminVerifyAccounts(ctx, sc, admin);
                case "2" -> adminViewUnmatched(admin);
                case "3" -> adminViewActiveDonations(admin, ctx);
                case "4" -> adminViewAuditLog(ctx, sc, admin);
                case "5" -> adminSearchShelter(sc, admin);
                case "6" -> adminSearchByName(ctx, sc, admin);
                case "7" -> adminFilterOrdersByStatus(ctx, sc, admin);
                case "8" -> adminFilterAuditByActor(ctx, sc, admin);
                case "9" -> adminFilterDonationByStatus(ctx, sc, admin);
                case "10" -> adminReviewEditRequests(ctx, sc, admin);
                case "11" -> adminUpdateDelivery(ctx, sc);
                case "12" -> adminViewRegisteredAccounts(ctx);
                case "13" -> adminViewActiveAlerts(ctx);
                case "L" -> {
                    admin.logout();
                    running = false;
                }
            }
        }
    }

    private static void adminViewDashboard(AppContext ctx, Admin admin) {
        admin.viewDashboard();

        List<FoodDonation> active = admin.viewActiveDonations();
        System.out.println("\n⏳ DONASI AKTIF (sisa waktu kesegaran):");
        if (active.isEmpty()) {
            System.out.println("   • (antrian kosong)");
        } else {
            for (FoodDonation d : active) {
                long menit = d.getRemainingMinutes();
                String alert = d.isInBuffer() ? " 🔴"
                        : menit <= SystemConfig.YELLOW_ALERT_MINUTES ? " 🟡" : "";
                System.out.printf("   • %s | %s | %d porsi | %d mnt%s%n",
                        d.getDonationId(), d.getFoodName(), d.getPortions(), menit, alert);
            }
        }

        List<FoodDonation> unmatched = admin.viewUnmatchedDonations();
        System.out.println("\n❌ TIDAK TERSALURKAN (penyebab):");
        if (unmatched.isEmpty()) {
            System.out.println("   • (semua sudah dicocokkan)");
        } else {
            for (FoodDonation d : unmatched) {
                long menit = d.getRemainingMinutes();
                String alasan;
                if (menit <= 0) {
                    alasan = "Sudah expired";
                } else if (menit <= SystemConfig.FRESHNESS_BUFFER_MIN) {
                    alasan = "Kritis (<" + SystemConfig.FRESHNESS_BUFFER_MIN + " mnt), radius diperluas "
                            + SystemConfig.EXPANDED_RADIUS_KM + " km";
                } else {
                    alasan = "Belum ada panti dalam radius " + SystemConfig.MAX_RADIUS_KM + " km";
                }
                System.out.printf("   • %s | %s | Alasan: %s%n",
                        d.getDonationId(), d.getFoodName(), alasan);
            }
        }

        LinkedList<User> pending = admin.viewPendingAccounts();
        System.out.println("\n🔑 AKUN PENDING:");
        if (pending.isEmpty()) {
            System.out.println("   • (tidak ada)");
        } else {
            for (User u : pending) {
                String type = u instanceof Restaurant ? "RESTORAN" : "PANTI";
                System.out.printf("   • [%s] %s | %s%n",
                        type, u.getUsername(), u.getRegisteredAt().format(DT_FMT));
            }
        }
        System.out.println();
    }

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
                System.out.printf(
                        "    Pemilik: %s | HP: %s | Daftar: %s%n    Alamat: %s%n    Lat/Lon: %.4f, %.4f | Kategori: %s%n",
                        r.getOwnerName(), r.getPhone(), r.getRegisteredAt().format(DT_FMT),
                        r.getAddress(), r.getLat(), r.getLon(), r.getFoodCategory());
            else if (u instanceof Shelter s)
                System.out.printf(
                        "    Pengurus: %s | HP: %s | Daftar: %s%n    Alamat: %s%n    Penghuni: %d | Tipe: %s%n",
                        s.getManagerName(), s.getPhone(), s.getRegisteredAt().format(DT_FMT),
                        s.getAddress(), s.getResidents(), s.getShelterType());
            System.out.println();
        }

        int choice = FoodSaverApp.readIndexOrCancel(sc, "Pilih nomor akun (0=kembali): ", pending.size());
        if (choice == 0)
            return;

        User target = pending.get(choice - 1);
        System.out.println("\n=== VERIFIKASI AKUN ===");
        System.out.println("Panduan: Cek di Google Maps, konfirmasi alamat, hubungi HP jika perlu.");
        String decision = FoodSaverApp.readMenuChoice(sc, "Keputusan [APPROVED/REJECTED/SKIP]: ",
                "APPROVED", "REJECTED", "SKIP");
        if (decision.equals("SKIP"))
            return;

        System.out.print("Catatan (opsional): ");
        String notes = sc.nextLine();
        admin.verifyAccount(target, decision, notes);
    }

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

    private static void adminViewActiveDonations(Admin admin, AppContext ctx) {
        List<FoodDonation> active = admin.viewActiveDonations();
        if (active.isEmpty()) {
            System.out.println("[!] Antrian donasi kosong.");
            return;
        }

        List<Shelter> shelters = new ArrayList<>(ctx.registry.getAll());

        FoodSaverApp.printHeader("STOK DONASI AKTIF");
        for (FoodDonation d : active) {
            Restaurant r = d.getRestaurant();
            System.out.printf("  %s | %-20s | %3d porsi | expired: %s | sisa: %d mnt%n",
                    d.getDonationId(), d.getFoodName(), d.getPortions(),
                    d.getExpiredAt().format(TM_FMT), d.getRemainingMinutes());

            if (r != null && !shelters.isEmpty()) {
                System.out.println("    Jarak ke panti (referensi prioritas):");
                shelters.sort(Comparator.comparingDouble(s -> GeoUtils.euclideanKm(
                        r.getLat(), r.getLon(), s.getLat(), s.getLon())));
                for (Shelter s : shelters) {
                    double km = GeoUtils.euclideanKm(r.getLat(), r.getLon(),
                            s.getLat(), s.getLon());
                    System.out.printf("      %.2f km → %s%n", km, s.getName());
                }
            }
        }
    }

    private static void adminViewRegisteredAccounts(AppContext ctx) {
        FoodSaverApp.printHeader("DAFTAR RESTORAN & PANTI TERDAFTAR");

        List<Restaurant> restaurants = new ArrayList<>();
        List<Shelter> shelters = new ArrayList<>();

        for (User u : ctx.userMap.values()) {
            if (u instanceof Restaurant r) {
                restaurants.add(r);
            } else if (u instanceof Shelter s) {
                shelters.add(s);
            }
        }

        if (restaurants.isEmpty() && shelters.isEmpty()) {
            System.out.println("  (Belum ada restoran atau panti terdaftar)");
            return;
        }

        if (!restaurants.isEmpty()) {
            restaurants.sort(Comparator.comparing(Restaurant::getName));
            System.out.println("  🍽️  RESTORAN TERDAFTAR:");
            for (Restaurant r : restaurants) {
                String nameUser = r.getName() + " (" + r.getUsername() + ")";
                System.out.printf("    - %-45s | %-37s | Status: %s%n",
                        nameUser, r.getAddress(), r.getAccountStatus());
            }
        } else {
            System.out.println("  (Tidak ada restoran terdaftar)");
        }

        if (!shelters.isEmpty()) {
            shelters.sort(Comparator.comparing(Shelter::getName));
            System.out.println("\n  🏠  PANTI TERDAFTAR:");
            for (Shelter s : shelters) {
                String nameUser = s.getName() + " (" + s.getUsername() + ")";
                System.out.printf("    - %-45s | %-37s | Penghuni: %3d | Status: %s%n",
                        nameUser, s.getAddress(), s.getResidents(), s.getAccountStatus());
            }
        } else {
            System.out.println("  (Tidak ada panti terdaftar)");
        }
    }

    private static void adminSearchShelter(Scanner sc, Admin admin) {
        String id = FoodSaverApp.readNonEmpty(sc, "Masukkan Shelter ID: ");
        Shelter s = admin.searchShelterById(id);
        if (s == null) {
            System.out.println("[✗] Panti tidak ditemukan.");
            return;
        }
        System.out.printf("[✓] %s | Penghuni: %d | Kebutuhan: %d | Status: %s%n",
                s.getName(), s.getResidents(), s.getRemainingNeed(), s.getAccountStatus());
    }

    private static void adminSearchByName(AppContext ctx, Scanner sc, Admin admin) {
        FoodSaverApp.printHeader("PENCARIAN BERDASARKAN NAMA");
        System.out.println("  [1] Cari restoran by nama");
        System.out.println("  [2] Cari donasi by nama makanan");
        System.out.println("  [0] Kembali");
        FoodSaverApp.printDivider();
        String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ", "1", "2", "0");

        switch (ch) {
            case "1" -> {
                String keyword = FoodSaverApp.readNonEmpty(sc, "Nama restoran (sebagian/penuh): ");
                List<Restaurant> results = admin.searchRestaurantByName(keyword);
                if (results.isEmpty()) {
                    System.out.println("[!] Tidak ada restoran yang cocok dengan \"" + keyword + "\".");
                    return;
                }
                FoodSaverApp.printHeader("HASIL PENCARIAN RESTORAN — \"" + keyword + "\"");
                for (Restaurant r : results)
                    System.out.printf("  %s | %-25s | Pemilik: %-15s | Status: %s%n",
                            r.getUserId(), r.getName(), r.getOwnerName(), r.getAccountStatus());
            }
            case "2" -> {
                String keyword = FoodSaverApp.readNonEmpty(sc, "Nama makanan (sebagian/penuh): ");
                List<FoodDonation> results = admin.searchDonationByName(keyword);
                if (results.isEmpty()) {
                    System.out.println("[!] Tidak ada donasi yang cocok dengan \"" + keyword + "\".");
                    return;
                }
                FoodSaverApp.printHeader("HASIL PENCARIAN DONASI — \"" + keyword + "\"");
                for (FoodDonation d : results)
                    System.out.printf("  %s | %-25s | %3d porsi | status: %-12s | dari: %s%n",
                            d.getDonationId(), d.getFoodName(), d.getPortions(),
                            d.getStatus(), d.getRestaurant().getName());
            }
        }
    }

    private static void adminFilterOrdersByStatus(AppContext ctx, Scanner sc, Admin admin) {
        FoodSaverApp.printHeader("FILTER ORDER BERDASARKAN STATUS");
        System.out.println("  [1] WAITING_PICKUP");
        System.out.println("  [2] PICKED_UP");
        System.out.println("  [3] IN_TRANSIT");
        System.out.println("  [4] DELIVERED");
        System.out.println("  [5] CANCELLED");
        System.out.println("  [0] Kembali");
        FoodSaverApp.printDivider();
        String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ", "1", "2", "3", "4", "5", "0");
        if (ch.equals("0"))
            return;

        OrderStatus status = switch (ch) {
            case "2" -> OrderStatus.PICKED_UP;
            case "3" -> OrderStatus.IN_TRANSIT;
            case "4" -> OrderStatus.DELIVERED;
            case "5" -> OrderStatus.CANCELLED;
            default -> OrderStatus.WAITING_PICKUP;
        };

        List<DeliveryOrder> results = admin.filterOrdersByStatus(status);
        if (results.isEmpty()) {
            System.out.println("[!] Tidak ada order dengan status " + status + ".");
            return;
        }
        FoodSaverApp.printHeader("ORDER — " + status);
        for (DeliveryOrder o : results)
            System.out.printf("  %s | Tujuan: %-30s | Porsi: %3d | Rating: %d%n",
                    o.getOrderId(), o.getShelter().getName(),
                    o.getBundle().getTotalPortions(), o.getRating());
    }

    private static void adminViewAuditLog(AppContext ctx, Scanner sc, Admin admin) {
        FoodSaverApp.printHeader("AUDIT LOG");
        System.out.println("  [1] Semua entri");
        System.out.println("  [2] Hanya ALERT & EXPIRED & WASTED");
        System.out.println("  [3] Telusuri lifecycle satu donasi (by ID)");
        System.out.println("  [0] Kembali");
        FoodSaverApp.printDivider();
        String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ", "1", "2", "3", "0");
        if (ch.equals("0"))
            return;

        if (ch.equals("3")) {
            String donationId = FoodSaverApp.readNonEmpty(sc, "Masukkan ID donasi: ");
            List<AuditEntry> trace = ctx.auditLog.traceDonationLifecycle(donationId);
            if (trace.isEmpty()) {
                System.out.println("[!] Tidak ada riwayat untuk donasi ID: " + donationId);
                return;
            }
            FoodSaverApp.printHeader("LIFECYCLE DONASI — " + donationId);
            for (AuditEntry e : trace)
                System.out.println(e);
            return;
        }

        List<AuditEntry> entries = ch.equals("2")
                ? ctx.auditLog.filterExpiredAndWasted()
                : ctx.auditLog.getAll();

        if (entries.isEmpty()) {
            System.out.println("[!] Tidak ada entri audit.");
            return;
        }
        FoodSaverApp.printHeader("AUDIT LOG" + (ch.equals("2") ? " — ALERT/EXPIRED/WASTED" : " — SEMUA"));
        for (AuditEntry e : entries)
            System.out.println(e);
    }

    private static void adminFilterAuditByActor(AppContext ctx, Scanner sc, Admin admin) {
        String username = FoodSaverApp.readNonEmpty(sc, "Masukkan username yang ingin dicari: ");
        List<AuditEntry> results = admin.filterAuditLogByActor(username);
        if (results.isEmpty()) {
            System.out.println("[!] Tidak ada entri audit untuk username \"" + username + "\".");
            return;
        }
        FoodSaverApp.printHeader("AUDIT LOG — " + username);
        for (AuditEntry e : results)
            System.out.println(e);
    }

    private static void adminFilterDonationByStatus(AppContext ctx, Scanner sc, Admin admin) {
        FoodSaverApp.printHeader("FILTER DONASI BERDASARKAN STATUS");
        System.out.println("  [1] EXPIRED_UNDELIVERED");
        System.out.println("  [2] WASTED");
        System.out.println("  [0] Kembali");
        FoodSaverApp.printDivider();
        String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ", "1", "2", "0");
        if (ch.equals("0"))
            return;

        DonationStatus status = switch (ch) {
            case "2" -> DonationStatus.WASTED;
            default -> DonationStatus.EXPIRED_UNDELIVERED;
        };

        List<FoodDonation> results = admin.filterDonationByStatus(status);
        if (results.isEmpty()) {
            System.out.println("[!] Tidak ada donasi dengan status " + status + ".");
            return;
        }
        FoodSaverApp.printHeader("DONASI — " + status);
        for (FoodDonation d : results)
            System.out.printf("  %s | %-20s | %3d porsi | dari: %s%n",
                    d.getDonationId(), d.getFoodName(),
                    d.getPortions(), d.getRestaurant().getName());
    }

    private static void adminViewActiveAlerts(AppContext ctx) {
        FoodSaverApp.printHeader("STATUS ALERT AKTIF");

        List<FoodDonation> allDonations = ctx.pool.getAll();

        List<FoodDonation> redAlerts = new ArrayList<>();
        List<FoodDonation> yellowAlerts = new ArrayList<>();

        for (FoodDonation d : allDonations) {
            if (d.isInBuffer()) {
                redAlerts.add(d);
            } else if (d.getRemainingMinutes() <= util.SystemConfig.YELLOW_ALERT_MINUTES
                    && d.getStatus() == enums.DonationStatus.WAITING) {
                yellowAlerts.add(d);
            }
        }

        if (redAlerts.isEmpty() && yellowAlerts.isEmpty()) {
            System.out.println("  [✓] Tidak ada alert aktif saat ini.");
            return;
        }

        if (!redAlerts.isEmpty()) {
            System.out.println("\n  🔴 RED ALERT — donasi dalam buffer 30 menit terakhir:");
            for (FoodDonation d : redAlerts) {
                System.out.printf("    %s | %-20s | sisa ~%d menit | expired: %s%n",
                        d.getDonationId(), d.getFoodName(),
                        d.getRemainingMinutes(),
                        d.getExpiredAt().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
            }
        }

        if (!yellowAlerts.isEmpty()) {
            System.out.println("\n  🟡 YELLOW ALERT — donasi < 2 jam sebelum expired:");
            for (FoodDonation d : yellowAlerts) {
                System.out.printf("    %s | %-20s | sisa ~%d menit | expired: %s%n",
                        d.getDonationId(), d.getFoodName(),
                        d.getRemainingMinutes(),
                        d.getExpiredAt().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
            }
        }
    }

    private static void adminReviewEditRequests(AppContext ctx, Scanner sc, Admin admin) {
        List<User> pending = admin.viewPendingEdits();
        if (pending.isEmpty()) {
            System.out.println("[!] Tidak ada permintaan edit profil yang menunggu.");
            return;
        }

        FoodSaverApp.printHeader("PERMINTAAN EDIT PROFIL");
        for (int i = 0; i < pending.size(); i++) {
            User u = pending.get(i);
            String type = u instanceof Restaurant ? "RESTORAN" : "PANTI";
            EditRequest req = u.getPendingEditRequest();
            System.out.printf("[%d] %s - %s%n", i + 1, type, u.getUsername());
            System.out.printf("    Dikirim: %s%n",
                    req.getRequestedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            System.out.println("    Perubahan yang diminta:");
            for (String line : req.getChangeSummary())
                System.out.println("      - " + line);
            System.out.println();
        }

        int choice = FoodSaverApp.readIndexOrCancel(sc, "Pilih nomor (0=kembali): ", pending.size());
        if (choice == 0)
            return;

        User target = pending.get(choice - 1);
        System.out.println("\n=== REVIEW PERMINTAAN EDIT ===");
        System.out.println("Panduan: Verifikasi data baru sebelum menyetujui.");
        String decision = FoodSaverApp.readMenuChoice(sc,
                "Keputusan [APPROVED/REJECTED/SKIP]: ", "APPROVED", "REJECTED", "SKIP");
        if (decision.equals("SKIP"))
            return;

        System.out.print("Catatan (opsional): ");
        String notes = sc.nextLine();
        admin.applyEditRequest(target, decision, notes);
    }

    private static void adminUpdateDelivery(AppContext ctx, Scanner sc) {
        List<DeliveryOrder> orders = ctx.history.getAll();
        if (orders.isEmpty()) {
            System.out.println("[!] Belum ada order.");
            return;
        }

        FoodSaverApp.printHeader("UPDATE STATUS PENGIRIMAN");
        List<DeliveryOrder> active = new ArrayList<>();
        for (DeliveryOrder o : orders) {
            if (o.getStatus() != OrderStatus.DELIVERED && o.getStatus() != OrderStatus.CANCELLED) {
                active.add(o);
            }
        }
        if (active.isEmpty()) {
            System.out.println("[!] Tidak ada order yang aktif.");
            return;
        }

        for (int i = 0; i < active.size(); i++) {
            DeliveryOrder o = active.get(i);
            System.out.printf("[%d] %s | Panti: %-25s | Status: %s%n",
                    i + 1, o.getOrderId(), o.getShelter().getName(), o.getStatus());
        }

        int choice = FoodSaverApp.readIndexOrCancel(sc, "Pilih nomor order (0=batal): ", active.size());
        if (choice == 0)
            return;

        DeliveryOrder order = active.get(choice - 1);
        System.out.println("Status saat ini : " + order.getStatus());
        System.out.println("Status berikutnya: " + nextStatus(order.getStatus()));

        if (FoodSaverApp.readYesNo(sc, "Maju ke status berikutnya? [Y/N]: ")) {
            order.advanceStatus();
            ctx.auditLog.log("ADMIN", ActionType.STATUS_UPDATE,
                    order.getOrderId(), "Status -> " + order.getStatus());
            System.out.println("[✓] Status pengiriman diperbarui.");

            for (FoodDonation d : order.getBundle().getDonations()) {
                if (d.getStatus() == enums.DonationStatus.WASTED) {
                    ctx.auditLog.log(new AuditEntry("SYSTEM", ActionType.WASTED,
                            d.getDonationId(),
                            "Wasted during IN_TRANSIT on order " + order.getOrderId(),
                            enums.DonationStatus.WASTED));
                    ctx.history.addWasted(d);
                }
            }

            System.out.println("\n--- Timeline Status ---");
            for (String s : order.getStatusTimeline())
                System.out.println("  " + s);

            System.out.println("\n--- Kesegaran Donasi dalam Bundle ---");
            for (FoodDonation d : order.getBundle().getDonations()) {
                long menit = d.getRemainingMinutes();
                System.out.printf("  %s | %-20s | Sisa: %d menit | Status: %s%n",
                        d.getDonationId(), d.getFoodName(), menit, d.getStatus());
            }
        }

        if (order.getStatus() == OrderStatus.IN_TRANSIT) {
            System.out.println("[i] Order sudah IN_TRANSIT. Konfirmasi penerimaan dilakukan oleh panti.");
            return;
        }
    }

    private static OrderStatus nextStatus(OrderStatus current) {
        return switch (current) {
            case WAITING_PICKUP -> OrderStatus.PICKED_UP;
            case PICKED_UP -> OrderStatus.IN_TRANSIT;
            default -> current;
        };
    }

    public static void showRestaurant(AppContext ctx, Scanner sc, Restaurant restaurant) {
        boolean running = true;
        while (running) {
            FoodSaverApp.printHeader("MENU RESTORAN — " + restaurant.getName());
            System.out.println("  [1] Posting donasi makanan");
            System.out.println("  [2] Riwayat donasi saya");
            System.out.println("  [3] Edit porsi donasi");
            System.out.println("  [4] Batalkan donasi");
            System.out.println("  [5] Edit profil restoran");
            System.out.println("  [0] Logout");
            FoodSaverApp.printDivider();
            String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ", "1", "2", "3", "4", "5", "0");
            switch (ch) {
                case "1" -> restaurantPostDonation(ctx, sc, restaurant);
                case "2" -> restaurantViewHistory(restaurant);
                case "3" -> restaurantEditPortions(ctx, sc, restaurant);
                case "4" -> restaurantCancelDonation(ctx, sc, restaurant);
                case "5" -> restaurantEditProfile(ctx, sc, restaurant);
                case "0" -> {
                    restaurant.logout();
                    running = false;
                }
            }
        }
    }

    private static void restaurantEditProfile(AppContext ctx, Scanner sc, Restaurant restaurant) {
        if (restaurant.hasPendingEdit()) {
            System.out.println("[!] Kamu sudah punya permintaan edit yang sedang menunggu persetujuan admin.");
            System.out.println("    Tunggu hingga admin memproses permintaan sebelumnya.");
            return;
        }

        FoodSaverApp.printHeader("EDIT PROFIL RESTORAN");
        System.out.println("  Kosongkan field untuk mempertahankan nilai lama.\n");

        System.out.printf("  Nama restoran saat ini   : %s%n", restaurant.getName());
        System.out.printf("  Nama pemilik saat ini    : %s%n", restaurant.getOwnerName());
        System.out.printf("  Alamat saat ini          : %s%n", restaurant.getAddress());
        System.out.printf("  Koordinat saat ini       : %.4f, %.4f%n", restaurant.getLat(), restaurant.getLon());
        System.out.printf("  Jenis makanan saat ini   : %s%n", restaurant.getFoodCategory());
        System.out.printf("  No. HP saat ini          : %s%n", restaurant.getPhone());
        System.out.println();

        java.util.Map<String, String> newData = new java.util.HashMap<>();

        System.out.print("  Nama restoran baru       : ");
        String v = sc.nextLine().trim();
        if (!v.isEmpty())
            newData.put("name", v);

        System.out.print("  Nama pemilik baru        : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty())
            newData.put("owner", v);

        System.out.print("  Alamat baru              : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty())
            newData.put("address", v);

        System.out.print("  Koordinat lat baru       : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            try {
                Double.parseDouble(v);
                newData.put("lat", v);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Lat tidak valid, dilewati.");
            }
        }

        System.out.print("  Koordinat lon baru       : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            try {
                Double.parseDouble(v);
                newData.put("lon", v);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Lon tidak valid, dilewati.");
            }
        }

        System.out.print("  Jenis makanan baru       : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty())
            newData.put("category", v);

        System.out.print("  No. HP baru              : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty())
            newData.put("phone", v);

        System.out.print("  Password baru (kosongkan jika tidak diubah): ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            if (v.length() < 6)
                System.out.println("  [!] Password min. 6 karakter, dilewati.");
            else
                newData.put("password", v);
        }

        if (newData.isEmpty()) {
            System.out.println("[!] Tidak ada perubahan yang dimasukkan.");
            return;
        }

        EditRequest req = new EditRequest(restaurant, newData);
        restaurant.setPendingEditRequest(req);
        ctx.auditLog.log(restaurant.getUsername(), ActionType.EDIT_REQUEST,
                restaurant.getUserId(), "Edit profile request: " + newData.keySet());
        System.out.println("\n[✓] Permintaan edit profil telah dikirim. Menunggu persetujuan admin.");
        System.out.println("    Field yang diminta diubah: " + newData.keySet());
    }

    private static void restaurantPostDonation(AppContext ctx, Scanner sc, Restaurant restaurant) {
        FoodSaverApp.printHeader("POSTING DONASI BARU");

        String foodName = FoodSaverApp.readNonEmpty(sc, "Jenis makanan           : ");
        int portions = FoodSaverApp.readPositiveInt(sc, "Jumlah porsi            : ");
        String timeStr = FoodSaverApp.readTime(sc, "Waktu masak (HH:MM)     : ");
        String[] parts = timeStr.split(":");
        LocalDateTime cookedAt = LocalDateTime.now()
                .withHour(Integer.parseInt(parts[0])).withMinute(Integer.parseInt(parts[1]))
                .withSecond(0).withNano(0);
        if (cookedAt.isAfter(LocalDateTime.now()))
            cookedAt = cookedAt.minusDays(1);

        System.out.print("Catatan tambahan        : ");
        String notes = sc.nextLine();

        LocalDateTime now = LocalDateTime.now();
        long minutesSinceCooked = java.time.Duration.between(cookedAt, now).getSeconds() / 60;
        long maxMinutes = SystemConfig.MAX_FRESH_HOURS * 60L;

        System.out.println("\n  [✓] Waktu sekarang  : " + now.format(TM_FMT));
        System.out.println("  [✓] Waktu masak     : " + cookedAt.format(TM_FMT));
        System.out.printf("  [✓] Selisih         : %d menit (batas %d jam)%n", minutesSinceCooked,
                SystemConfig.MAX_FRESH_HOURS);

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

    private static void restaurantViewHistory(Restaurant restaurant) {
        List<FoodDonation> history = restaurant.viewDonationHistory();
        if (history.isEmpty()) {
            System.out.println("[!] Belum ada riwayat donasi.");
            return;
        }
        FoodSaverApp.printHeader("RIWAYAT DONASI — " + restaurant.getName());
        for (FoodDonation d : history)
            System.out.printf("  %s | %-20s | %3d porsi | status: %s%n",
                    d.getDonationId(), d.getFoodName(), d.getPortions(), d.getStatus());
    }

    private static void restaurantEditPortions(AppContext ctx, Scanner sc, Restaurant restaurant) {
        String id = FoodSaverApp.readNonEmpty(sc, "Masukkan Donation ID: ");
        int newPortions = FoodSaverApp.readPositiveInt(sc, "Jumlah porsi baru   : ");
        boolean updated = restaurant.editPortions(id, newPortions);
        if (updated) {

            System.out.println("\n[MatchingEngine] Porsi diperbarui — menjalankan ulang matching...");
            ctx.engine.run();
            ctx.pool.checkAlerts();
        }
    }

    private static void restaurantCancelDonation(AppContext ctx, Scanner sc, Restaurant restaurant) {
        String id = FoodSaverApp.readNonEmpty(sc, "Masukkan Donation ID yang akan dibatalkan: ");
        restaurant.cancelDonation(id);
        List<FoodDonation> poolList = ctx.pool.getAll();
        for (FoodDonation d : poolList) {
            if (d.getDonationId().equals(id)) {
                ctx.pool.remove(d);
                ctx.expiryTree.remove(d);
                break;
            }
        }
    }

    public static void showShelter(AppContext ctx, Scanner sc, Shelter shelter) {
        boolean running = true;
        while (running) {
            long pending = getShelterIncomingOrders(ctx, shelter).size();
            FoodSaverApp.printHeader("MENU PANTI — " + shelter.getName());
            System.out.printf("  [1] Konfirmasi donasi masuk%s%n",
                    pending > 0 ? "   [ADA " + pending + " MENUNGGU]" : "");
            System.out.println("  [2] Riwayat penerimaan");
            System.out.println("  [3] Perbarui jumlah penghuni");
            System.out.println("  [4] Edit profil panti");
            System.out.println("  [0] Logout");
            FoodSaverApp.printDivider();
            String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ", "1", "2", "3", "4", "0");
            switch (ch) {
                case "1" -> shelterConfirmDonation(ctx, sc, shelter);
                case "2" -> shelterViewHistory(ctx, shelter);
                case "3" -> shelterUpdateResidents(sc, shelter);
                case "4" -> shelterEditProfile(ctx, sc, shelter);
                case "0" -> {
                    shelter.logout();
                    running = false;
                }
            }
        }
    }

    private static List<DeliveryOrder> getShelterIncomingOrders(AppContext ctx, Shelter shelter) {
        List<DeliveryOrder> result = new ArrayList<>();
        for (DeliveryOrder o : ctx.history.getAll()) {
            if (o.getShelter().getUserId().equals(shelter.getUserId())
                    && o.getStatus() == OrderStatus.IN_TRANSIT) {
                result.add(o);
            }
        }
        return result;
    }

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
            System.out.printf("%n    Porsi  : %d | Surplus: %d | Status: %s%n",
                    o.getBundle().getTotalPortions(), o.getPortionSurplus(), o.getStatus());
        }

        int choice = FoodSaverApp.readIndexOrCancel(sc, "Pilih nomor order (0=batal): ", incoming.size());
        if (choice == 0)
            return;

        DeliveryOrder order = incoming.get(choice - 1);
        if (!FoodSaverApp.readYesNo(sc, "\nKonfirmasi diterima? [Y/N] : ")) {
            System.out.println("[!] Konfirmasi dibatalkan.");
            return;
        }

        int rating = FoodSaverApp.readIntInRange(sc, "Rating donasi (1–5)        : ", 1, 5);
        System.out.print("Catatan                    : ");
        String notes = sc.nextLine();
        shelter.confirmReceipt(order, rating, notes);
        ctx.auditLog.log(new AuditEntry(shelter.getUsername(), ActionType.DELIVER,
                order.getOrderId(), "Confirmed by shelter. Rating: " + rating,
                enums.DonationStatus.DELIVERED));

        int totalPortions = order.getBundle().getTotalPortions();
        int surplus = order.getPortionSurplus();
        int received = order.getPortionsReceived();

        int receivedToday = 0;
        for (DeliveryOrder o : shelter.viewReceiptHistory())
            if (o.getStatus() == OrderStatus.DELIVERED)
                receivedToday += o.getPortionsReceived();

        System.out.println("\n[✓] Penerimaan dikonfirmasi. Status order: DELIVERED");
        System.out.printf("    %-18s: %d / %d porsi bundle%n", "Porsi diterima", received, totalPortions);
        if (surplus > 0)
            System.out.printf("    %-18s: %d (melebihi kebutuhan panti)%n", "Porsi surplus", surplus);
        System.out.printf("    %-18s: %d / 5%n", "Rating diberikan", rating);
        if (!notes.isBlank())
            System.out.printf("    %-18s: %s%n", "Catatan", notes);
        System.out.printf("    %-18s: %d / %d porsi kebutuhan panti%n",
                "Kebutuhan hari ini", receivedToday, shelter.getResidents());
    }

    private static void shelterViewHistory(AppContext ctx, Shelter shelter) {
        List<DeliveryOrder> all = ctx.history.filterByShelter(shelter.getUserId());
        if (all.isEmpty()) {
            System.out.println("[!] Belum ada riwayat penerimaan.");
            return;
        }
        FoodSaverApp.printHeader("RIWAYAT PENERIMAAN — " + shelter.getName());
        System.out.printf("  %-16s | %-14s | %-7s | %-7s | %-6s | %-8s | Catatan%n",
                "Order ID", "Status", "Porsi", "Surplus", "Rating", "Tgl");
        System.out.println("  " + "─".repeat(80));

        int receivedToday = 0;
        for (DeliveryOrder o : all) {
            int totalPortions = o.getBundle().getTotalPortions();
            int surplus = o.getPortionSurplus();
            int received = o.getPortionsReceived();
            if (o.getStatus() == OrderStatus.DELIVERED)
                receivedToday += received;
            System.out.printf("  %-16s | %-14s | %3d/%-3d | %-7d | %-6d | %-8s | %s%n",
                    o.getOrderId(),
                    o.getStatus(),
                    received, totalPortions,
                    surplus,
                    o.getRating(),
                    o.getCreatedAt().format(TM_FMT),
                    o.getReceiptNotes().isBlank() ? "(tidak ada)" : o.getReceiptNotes());
            System.out.print("    └ Makanan: ");
            for (FoodDonation d : o.getBundle().getDonations())
                System.out.printf("[%s %d porsi] ", d.getFoodName(), d.getPortions());
            System.out.println();
        }
        System.out.printf("%n  Total diterima hari ini: %d / %d porsi kebutuhan panti%n",
                receivedToday, shelter.getResidents());
    }

    private static void shelterEditProfile(AppContext ctx, Scanner sc, Shelter shelter) {
        if (shelter.hasPendingEdit()) {
            System.out.println("[!] Kamu sudah punya permintaan edit yang sedang menunggu persetujuan admin.");
            System.out.println("    Tunggu hingga admin memproses permintaan sebelumnya.");
            return;
        }

        FoodSaverApp.printHeader("EDIT PROFIL PANTI");
        System.out.println("  Kosongkan field untuk mempertahankan nilai lama.\n");

        System.out.printf("  Nama panti saat ini      : %s%n", shelter.getName());
        System.out.printf("  Nama pengurus saat ini   : %s%n", shelter.getManagerName());
        System.out.printf("  Alamat saat ini          : %s%n", shelter.getAddress());
        System.out.printf("  Koordinat saat ini       : %.4f, %.4f%n", shelter.getLat(), shelter.getLon());
        System.out.printf("  Jumlah penghuni saat ini : %d%n", shelter.getResidents());
        System.out.printf("  Tipe panti saat ini      : %s%n", shelter.getShelterType());
        System.out.printf("  No. HP saat ini          : %s%n", shelter.getPhone());
        System.out.println();

        java.util.Map<String, String> newData = new java.util.HashMap<>();

        System.out.print("  Nama panti baru          : ");
        String v = sc.nextLine().trim();
        if (!v.isEmpty())
            newData.put("name", v);

        System.out.print("  Nama pengurus baru       : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty())
            newData.put("manager", v);

        System.out.print("  Alamat baru              : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty())
            newData.put("address", v);

        System.out.print("  Koordinat lat baru       : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            try {
                Double.parseDouble(v);
                newData.put("lat", v);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Lat tidak valid, dilewati.");
            }
        }

        System.out.print("  Koordinat lon baru       : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            try {
                Double.parseDouble(v);
                newData.put("lon", v);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Lon tidak valid, dilewati.");
            }
        }

        System.out.print("  Jumlah penghuni baru     : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            try {
                int n = Integer.parseInt(v);
                if (n > 0)
                    newData.put("residents", v);
                else
                    System.out.println("  [!] Harus > 0, dilewati.");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Bukan angka, dilewati.");
            }
        }

        System.out.println("  Tipe panti baru          : [1] Anak Yatim  [2] Lansia  [3] Disabilitas  [Enter] Lewati");
        System.out.print("  Pilihan                  : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            String type = switch (v) {
                case "2" -> "LANSIA";
                case "3" -> "DISABILITAS";
                case "1" -> "ANAK_YATIM";
                default -> null;
            };
            if (type != null)
                newData.put("type", type);
            else
                System.out.println("  [!] Pilihan tidak valid, dilewati.");
        }

        System.out.print("  No. HP baru              : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty())
            newData.put("phone", v);

        System.out.print("  Password baru (kosongkan jika tidak diubah): ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            if (v.length() < 6)
                System.out.println("  [!] Password min. 6 karakter, dilewati.");
            else
                newData.put("password", v);
        }

        if (newData.isEmpty()) {
            System.out.println("[!] Tidak ada perubahan yang dimasukkan.");
            return;
        }

        EditRequest req = new EditRequest(shelter, newData);
        shelter.setPendingEditRequest(req);
        ctx.auditLog.log(shelter.getUsername(), ActionType.EDIT_REQUEST,
                shelter.getUserId(), "Edit profile request: " + newData.keySet());
        System.out.println("\n[✓] Permintaan edit profil telah dikirim. Menunggu persetujuan admin.");
        System.out.println("    Field yang diminta diubah: " + newData.keySet());
    }

    private static void shelterUpdateResidents(Scanner sc, Shelter shelter) {
        int count = FoodSaverApp.readPositiveInt(sc, "Jumlah penghuni aktif saat ini: ");
        shelter.updateResidents(count);
    }
}
