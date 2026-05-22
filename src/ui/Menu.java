package ui;

import model.*;
import enums.*;
import util.SystemConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

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
            final String u = username;
            if (ctx.allUsers.stream().anyMatch(x -> x.getUsername().equals(u)))
                System.out.println("[✗] Username \"" + username + "\" sudah digunakan.");
            else
                break;
        }
        String password = FoodSaverApp.readPassword(sc, "Password          : ");

        Restaurant r = new Restaurant(name, owner, username, password, phone, address, lat, lon, category);
        ctx.allUsers.add(r);
        ctx.auditLog.logAction(username, ActionType.REGISTER, r.getUserId(), "Registered restaurant: " + name);

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
            final String u = username;
            if (ctx.allUsers.stream().anyMatch(x -> x.getUsername().equals(u)))
                System.out.println("[✗] Username \"" + username + "\" sudah digunakan.");
            else
                break;
        }
        String password = FoodSaverApp.readPassword(sc, "Password          : ");

        Shelter s = new Shelter(name, manager, username, password, phone, address, lat, lon, residents, type);
        ctx.allUsers.add(s);
        ctx.registry.register(s);
        ctx.auditLog.logAction(username, ActionType.REGISTER, s.getUserId(), "Registered shelter: " + name);

        System.out.println("\n[✓] Registrasi berhasil. Akun Anda sedang menunggu verifikasi admin.");
    }

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
                case "0" -> {
                    restaurant.logout();
                    running = false;
                }
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
            System.out.println("  [0] Logout");
            FoodSaverApp.printDivider();
            String ch = FoodSaverApp.readMenuChoice(sc, "Pilihan: ", "1", "2", "3", "0");
            switch (ch) {
                case "1" -> shelterConfirmDonation(ctx, sc, shelter);
                case "2" -> shelterViewHistory(ctx, shelter);
                case "3" -> shelterUpdateResidents(sc, shelter);
                case "0" -> {
                    shelter.logout();
                    running = false;
                }
            }
        }
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
        ctx.pool.getAll().stream()
                .filter(d -> d.getDonationId().equals(id)).findFirst()
                .ifPresent(d -> {
                    ctx.pool.remove(d);
                    ctx.expiryTree.remove(d);
                });
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
        if (incoming.isEmpty()) {
            System.out.println("[!] Tidak ada donasi yang menunggu konfirmasi.");
            return;
        }

        FoodSaverApp.printHeader("KONFIRMASI PENERIMAAN");
        for (int i = 0; i < incoming.size(); i++) {
            DeliveryOrder o = incoming.get(i);
            System.out.printf("[%d] Order: %s%n    Dari   : ", i + 1, o.getOrderId());
            o.getBundle().getRestaurantList().forEach(r -> System.out.print(r.getName() + " "));
            System.out.printf("%n    Porsi  : %d%n    Status : %s%n", o.getBundle().getTotalPortions(), o.getStatus());
        }

        int choice = FoodSaverApp.readIndexOrCancel(sc, "Pilih nomor order (0=batal): ", incoming.size());
        if (choice == 0)
            return;

        DeliveryOrder order = incoming.get(choice - 1);
        if (!FoodSaverApp.readYesNo(sc, "\nKonfirmasi diterima? [Y/N]: ")) {
            System.out.println("[!] Konfirmasi dibatalkan.");
            return;
        }

        int rating = FoodSaverApp.readIntInRange(sc, "Rating donasi (1–5)        : ", 1, 5);
        System.out.print("Catatan                    : ");
        String notes = sc.nextLine();

        shelter.confirmReceipt(order, rating, notes);
        ctx.auditLog.logAction(shelter.getUsername(), ActionType.DELIVER,
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

    private static void shelterUpdateResidents(Scanner sc, Shelter shelter) {
        int count = FoodSaverApp.readPositiveInt(sc, "Jumlah penghuni aktif saat ini: ");
        shelter.updateResidents(count);
    }
}
