package ui;

import enums.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

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
        ctx.userMap.put(s.getUsername(), s);
        ctx.registry.register(s);
        ctx.auditLog.log(username, ActionType.REGISTER, s.getUserId(), "Registered shelter: " + name);

        System.out.println("\n[✓] Registrasi berhasil. Akun Anda sedang menunggu verifikasi admin.");
    }

    public static void showAdmin(AppContext ctx, Scanner sc, Admin admin) {
        boolean running = true;
        while (running) {
            ctx.pool.checkAlerts();
            FoodSaverApp.printHeader("MENU ADMIN — " + admin.getUsername());
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
            System.out.println("  [0] Logout");
            FoodSaverApp.printDivider();
            String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ",
                    "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "0");
            switch (ch) {
                case "1"  -> adminVerifyAccounts(ctx, sc, admin);
                case "2"  -> adminViewUnmatched(admin);
                case "3"  -> adminViewActiveDonations(admin, ctx);
                case "4"  -> adminViewAuditLog(ctx, sc, admin);
                case "5"  -> adminSearchShelter(sc, admin);
                case "6"  -> adminSearchByName(ctx, sc, admin);
                case "7"  -> adminFilterOrdersByStatus(ctx, sc, admin);
                case "8"  -> adminFilterAuditByActor(ctx, sc, admin);
                case "9"  -> adminFilterDonationByStatus(ctx, sc, admin);
                case "10" -> adminReviewEditRequests(ctx, sc, admin);
                case "0"  -> {
                    admin.logout();
                    running = false;
                }
            }
        }
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
                record ShelterDist(Shelter shelter, double km) {}
                List<ShelterDist> jarakList = new ArrayList<>();
                for (Shelter s : shelters) {
                    double km = GeoUtils.euclideanKm(r.getLat(), r.getLon(),
                            s.getLat(), s.getLon());
                    jarakList.add(new ShelterDist(s, km));
                }
                jarakList.sort(Comparator.comparingDouble(ShelterDist::km));
                for (ShelterDist sd : jarakList) {
                    System.out.printf("      %.2f km → %s%n", sd.km(), sd.shelter().getName());
                }
            }
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
                    System.out.printf("  %s | %-25s | Pemilik: %s | Status: %s%n",
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
                    System.out.printf("  %s | %-20s | %3d porsi | status: %-20s | dari: %s%n",
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
            System.out.printf("  %s | Tujuan: %-20s | Porsi: %3d | Rating: %d%n",
                    o.getOrderId(), o.getShelter().getName(),
                    o.getBundle().getTotalPortions(), o.getRating());
    }

    private static void adminViewAuditLog(AppContext ctx, Scanner sc, Admin admin) {
        FoodSaverApp.printHeader("AUDIT LOG");
        System.out.println("  [1] Semua entri");
        System.out.println("  [2] Hanya ALERT & EXPIRED & WASTED");
        System.out.println("  [0] Kembali");
        FoodSaverApp.printDivider();
        String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ", "1", "2", "0");
        if (ch.equals("0")) return;

        List<AuditEntry> entries = ch.equals("2")
                ? ctx.auditLog.filterExpiredAndWasted()
                : ctx.auditLog.getAll();

        if (entries.isEmpty()) {
            System.out.println("[!] Tidak ada entri audit.");
            return;
        }
        FoodSaverApp.printHeader("AUDIT LOG" + (ch.equals("2") ? " — ALERT/EXPIRED/WASTED" : " — SEMUA"));
        entries.forEach(System.out::println);
    }

    private static void adminFilterAuditByActor(AppContext ctx, Scanner sc, Admin admin) {
        String username = FoodSaverApp.readNonEmpty(sc, "Masukkan username yang ingin dicari: ");
        List<AuditEntry> results = admin.filterAuditLogByActor(username);
        if (results.isEmpty()) {
            System.out.println("[!] Tidak ada entri audit untuk username \"" + username + "\".");
            return;
        }
        FoodSaverApp.printHeader("AUDIT LOG — " + username);
        results.forEach(System.out::println);
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

        List<model.FoodDonation> allDonations = ctx.pool.getAll();

        List<model.FoodDonation> redAlerts = new java.util.ArrayList<>();
        List<model.FoodDonation> yellowAlerts = new java.util.ArrayList<>();

        for (model.FoodDonation d : allDonations) {
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
            for (model.FoodDonation d : redAlerts) {
                System.out.printf("    %s | %-20s | sisa ~%d menit | expired: %s%n",
                        d.getDonationId(), d.getFoodName(),
                        d.getRemainingMinutes(),
                        d.getExpiredAt().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
            }
        }

        if (!yellowAlerts.isEmpty()) {
            System.out.println("\n  🟡 YELLOW ALERT — donasi < 2 jam sebelum expired:");
            for (model.FoodDonation d : yellowAlerts) {
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
            req.getNewData().forEach((k, v) -> {
                String label = switch (k) {
                    case "name"      -> "Nama";
                    case "owner"     -> "Nama pemilik";
                    case "manager"   -> "Nama pengurus";
                    case "address"   -> "Alamat";
                    case "lat"       -> "Koordinat lat";
                    case "lon"       -> "Koordinat lon";
                    case "category"  -> "Jenis makanan";
                    case "residents" -> "Jumlah penghuni";
                    case "type"      -> "Tipe panti";
                    case "phone"     -> "No. HP";
                    case "password"  -> "Password";
                    default          -> k;
                };
                String display = k.equals("password") ? "****" : v;
                System.out.printf("      - %s: %s%n", label, display);
            });
            System.out.println();
        }

        int choice = FoodSaverApp.readIndexOrCancel(sc, "Pilih nomor (0=kembali): ", pending.size());
        if (choice == 0) return;

        User target = pending.get(choice - 1);
        System.out.println("\n=== REVIEW PERMINTAAN EDIT ===");
        System.out.println("Panduan: Verifikasi data baru sebelum menyetujui.");
        String decision = FoodSaverApp.readMenuChoice(sc,
                "Keputusan [APPROVED/REJECTED/SKIP]: ", "APPROVED", "REJECTED", "SKIP");
        if (decision.equals("SKIP")) return;

        System.out.print("Catatan (opsional): ");
        String notes = sc.nextLine();
        admin.applyEditRequest(target, decision, notes);
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
                case "3" -> restaurantEditPortions(sc, restaurant);
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
        if (!v.isEmpty()) newData.put("name", v);

        System.out.print("  Nama pemilik baru        : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) newData.put("owner", v);

        System.out.print("  Alamat baru              : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) newData.put("address", v);

        System.out.print("  Koordinat lat baru       : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            try { Double.parseDouble(v); newData.put("lat", v); }
            catch (NumberFormatException e) { System.out.println("  [!] Lat tidak valid, dilewati."); }
        }

        System.out.print("  Koordinat lon baru       : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            try { Double.parseDouble(v); newData.put("lon", v); }
            catch (NumberFormatException e) { System.out.println("  [!] Lon tidak valid, dilewati."); }
        }

        System.out.print("  Jenis makanan baru       : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) newData.put("category", v);

        System.out.print("  No. HP baru              : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) newData.put("phone", v);

        System.out.print("  Password baru (kosongkan jika tidak diubah): ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            if (v.length() < 6) System.out.println("  [!] Password min. 6 karakter, dilewati.");
            else newData.put("password", v);
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
        long minutesSinceCooked = java.time.temporal.ChronoUnit.MINUTES.between(cookedAt, now);
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

    private static void restaurantEditPortions(Scanner sc, Restaurant restaurant) {
        String id = FoodSaverApp.readNonEmpty(sc, "Masukkan Donation ID: ");
        int newPortions = FoodSaverApp.readPositiveInt(sc, "Jumlah porsi baru   : ");
        restaurant.editPortions(id, newPortions);
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
            String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ", "1","2","3","4","0");
            switch (ch) {
                case "1" -> shelterConfirmDonation(ctx, sc, shelter);
                case "2" -> shelterViewHistory(ctx, shelter);
                case "3" -> shelterUpdateResidents(sc, shelter);
                case "4" -> shelterEditProfile(ctx, sc, shelter);
                case "0" -> { shelter.logout(); running = false; }
            }
        }
    }

    private static List<DeliveryOrder> getShelterIncomingOrders(AppContext ctx, Shelter shelter) {
        return ctx.history.getAll().stream()
                .filter(o -> o.getShelter().getUserId().equals(shelter.getUserId()))
                .filter(o -> o.getStatus() == OrderStatus.IN_TRANSIT
                          || o.getStatus() == OrderStatus.WAITING_PICKUP
                          || o.getStatus() == OrderStatus.PICKED_UP)
                .collect(Collectors.toList());
    }

    private static void shelterConfirmDonation(AppContext ctx, Scanner sc, Shelter shelter) {
        List<DeliveryOrder> incoming = getShelterIncomingOrders(ctx, shelter);
        if (incoming.isEmpty()) { System.out.println("[!] Tidak ada donasi yang menunggu konfirmasi."); return; }

        FoodSaverApp.printHeader("KONFIRMASI PENERIMAAN");
        for (int i = 0; i < incoming.size(); i++) {
            DeliveryOrder o = incoming.get(i);
            System.out.printf("[%d] Order: %s%n    Dari   : ", i + 1, o.getOrderId());
            o.getBundle().getRestaurantList().forEach(r -> System.out.print(r.getName() + " "));
            System.out.printf("%n    Porsi  : %d%n    Status : %s%n", o.getBundle().getTotalPortions(), o.getStatus());
        }

        int choice = FoodSaverApp.readIndexOrCancel(sc, "Pilih nomor order (0=batal): ", incoming.size());
        if (choice == 0) return;

        DeliveryOrder order = incoming.get(choice - 1);
        if (!FoodSaverApp.readYesNo(sc, "\nKonfirmasi diterima? [Y/N]: ")) {
            System.out.println("[!] Konfirmasi dibatalkan."); return;
        }

        int    rating = FoodSaverApp.readIntInRange(sc, "Rating donasi (1–5)        : ", 1, 5);
        System.out.print("Catatan                    : ");
        String notes  = sc.nextLine();

        shelter.confirmReceipt(order, rating, notes);
        ctx.auditLog.log(shelter.getUsername(), ActionType.DELIVER,
                order.getOrderId(), "Confirmed by shelter. Rating: " + rating);
        System.out.println("[✓] Penerimaan dikonfirmasi. Status order: DELIVERED");
    }

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
        if (!v.isEmpty()) newData.put("name", v);

        System.out.print("  Nama pengurus baru       : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) newData.put("manager", v);

        System.out.print("  Alamat baru              : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) newData.put("address", v);

        System.out.print("  Koordinat lat baru       : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            try { Double.parseDouble(v); newData.put("lat", v); }
            catch (NumberFormatException e) { System.out.println("  [!] Lat tidak valid, dilewati."); }
        }

        System.out.print("  Koordinat lon baru       : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            try { Double.parseDouble(v); newData.put("lon", v); }
            catch (NumberFormatException e) { System.out.println("  [!] Lon tidak valid, dilewati."); }
        }

        System.out.print("  Jumlah penghuni baru     : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            try {
                int n = Integer.parseInt(v);
                if (n > 0) newData.put("residents", v);
                else System.out.println("  [!] Harus > 0, dilewati.");
            } catch (NumberFormatException e) { System.out.println("  [!] Bukan angka, dilewati."); }
        }

        System.out.println("  Tipe panti baru: [1] Anak Yatim  [2] Lansia  [3] Disabilitas  [Enter] Lewati");
        System.out.print("  Pilihan: ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            String type = switch (v) {
                case "2" -> "LANSIA";
                case "3" -> "DISABILITAS";
                case "1" -> "ANAK_YATIM";
                default -> null;
            };
            if (type != null) newData.put("type", type);
            else System.out.println("  [!] Pilihan tidak valid, dilewati.");
        }

        System.out.print("  No. HP baru              : ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) newData.put("phone", v);

        System.out.print("  Password baru (kosongkan jika tidak diubah): ");
        v = sc.nextLine().trim();
        if (!v.isEmpty()) {
            if (v.length() < 6) System.out.println("  [!] Password min. 6 karakter, dilewati.");
            else newData.put("password", v);
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
