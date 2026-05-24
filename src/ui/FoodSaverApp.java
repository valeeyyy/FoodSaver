package ui;

import enums.AccountStatus;
import java.util.Scanner;
import model.*;

public class FoodSaverApp {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        AppContext ctx = new AppContext();
        ctx.startup();

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = readMenuChoice(sc, "Pilihan: ", "1", "2", "0");
            switch (choice) {
                case "1" -> loginFlow(sc, ctx);
                case "2" -> Menu.showRegistration(ctx, sc);
                case "0" -> {
                    System.out.println("\n[✓] Terima kasih telah menggunakan FoodSaver. Sampai jumpa!");
                    running = false;
                }
            }
        }
        sc.close();
    }

    private static void printMainMenu() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║         🍱  F O O D S A V E R        ║");
        System.out.println("║   Distribusi Surplus Pangan Otomatis ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║  [1] Login                           ║");
        System.out.println("║  [2] Daftar Akun Baru                ║");
        System.out.println("║  [0] Keluar                          ║");
        System.out.println("╚══════════════════════════════════════╝");
    }

    private static void loginFlow(Scanner sc, AppContext ctx) {
        printHeader("LOGIN");
        String username = readNonEmpty(sc, "Username : ");
        String password = readNonEmpty(sc, "Password : ");

        User user = ctx.findUser(username, password);
        if (user == null) {
            System.out.println("[✗] Username atau password salah.");
            return;
        }
        if (user.getAccountStatus() == AccountStatus.REJECTED) {
            System.out.println("[✗] Akun Anda telah ditolak oleh admin. Silakan hubungi admin.");
            return;
        }
        if (user.getAccountStatus() == AccountStatus.PENDING) {
            System.out.println("[!] Akun Anda masih menunggu verifikasi admin.");
            return;
        }

        System.out.println("[✓] Login berhasil. Selamat datang, " + username + "!");

        if (user instanceof Admin admin)
            Menu.showAdmin(ctx, sc, admin);
        else if (user instanceof Restaurant restaurant)
            Menu.showRestaurant(ctx, sc, restaurant);
        else if (user instanceof Shelter shelter)
            Menu.showShelter(ctx, sc, shelter);
        else
            System.out.println("[✗] Jenis pengguna tidak dikenali.");
    }

    public static String readNonEmpty(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String v = sc.nextLine().trim();
            if (!v.isEmpty()) return v;
            System.out.println("[✗] Input tidak boleh kosong. Silakan coba lagi.");
        }
    }

    public static int readPositiveInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) { System.out.println("[✗] Input tidak boleh kosong."); continue; }
            try {
                int v = Integer.parseInt(raw);
                if (v > 0) return v;
                System.out.println("[✗] Angka harus lebih dari 0.");
            } catch (NumberFormatException e) {
                System.out.println("[✗] \"" + raw + "\" bukan angka bulat yang valid.");
            }
        }
    }

    public static int readIntInRange(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) { System.out.printf("[✗] Masukkan angka %d–%d.%n", min, max); continue; }
            try {
                int v = Integer.parseInt(raw);
                if (v >= min && v <= max) return v;
                System.out.printf("[✗] Angka harus antara %d–%d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("[✗] \"" + raw + "\" bukan angka bulat yang valid.");
            }
        }
    }

    public static double readDoubleInRange(Scanner sc, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) { System.out.printf("[✗] Masukkan desimal %.2f–%.2f.%n", min, max); continue; }
            try {
                double v = Double.parseDouble(raw);
                if (v >= min && v <= max) return v;
                System.out.printf("[✗] Nilai harus antara %.2f dan %.2f.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("[✗] Gunakan titik (.) sebagai pemisah desimal.");
            }
        }
    }

    public static String readTime(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) { System.out.println("[✗] Masukkan waktu format HH:MM."); continue; }
            String[] parts = raw.split(":");
            if (parts.length != 2) { System.out.println("[✗] Format HH:MM. Contoh: 14:30."); continue; }
            try {
                int h = Integer.parseInt(parts[0]), m = Integer.parseInt(parts[1]);
                if (h < 0 || h > 23) { System.out.println("[✗] Jam harus 00–23."); continue; }
                if (m < 0 || m > 59) { System.out.println("[✗] Menit harus 00–59."); continue; }
                return raw;
            } catch (NumberFormatException e) {
                System.out.println("[✗] Format salah. Contoh: 08:30.");
            }
        }
    }

    public static String readMenuChoice(Scanner sc, String prompt, String... validChoices) {
        while (true) {
            System.out.print(prompt);
            String raw = sc.nextLine().trim();
            for (String v : validChoices)
                if (v.equalsIgnoreCase(raw)) return raw.toUpperCase();
            System.out.println("[✗] Pilihan tidak valid. Pilih: " + String.join(", ", validChoices));
        }
    }

    public static boolean readYesNo(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = sc.nextLine().trim();
            if (raw.equalsIgnoreCase("Y")) return true;
            if (raw.equalsIgnoreCase("N")) return false;
            System.out.println("[✗] Masukkan Y atau N.");
        }
    }

    public static int readIndexOrCancel(Scanner sc, String prompt, int maxIndex) {
        while (true) {
            System.out.print(prompt);
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) { System.out.printf("[✗] Masukkan 0–%d.%n", maxIndex); continue; }
            try {
                int v = Integer.parseInt(raw);
                if (v == 0) return 0;
                if (v >= 1 && v <= maxIndex) return v;
                System.out.printf("[✗] Masukkan angka 0 (batal) hingga %d.%n", maxIndex);
            } catch (NumberFormatException e) {
                System.out.println("[✗] \"" + raw + "\" bukan angka yang valid.");
            }
        }
    }

    public static String readPhoneNumber(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) { System.out.println("[✗] Nomor HP tidak boleh kosong."); continue; }
            if (!raw.matches("[+]?[0-9][0-9\\-]{6,}")) {
                System.out.println("[✗] Nomor HP tidak valid. Contoh: 08123456789 (min. 8 digit).");
                continue;
            }
            return raw;
        }
    }

    public static String readUsername(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) { System.out.println("[✗] Username tidak boleh kosong."); continue; }
            if (raw.length() < 4) { System.out.println("[✗] Username minimal 4 karakter."); continue; }
            if (!raw.matches("[a-zA-Z0-9_]+")) {
                System.out.println("[✗] Username hanya boleh huruf, angka, dan underscore (_).");
                continue;
            }
            return raw;
        }
    }

    public static String readPassword(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = sc.nextLine();
            if (raw.isEmpty()) { System.out.println("[✗] Password tidak boleh kosong."); continue; }
            if (raw.length() < 6) { System.out.println("[✗] Password minimal 6 karakter."); continue; }
            return raw;
        }
    }

    public static void printHeader(String title) {
        System.out.println("\n" + "═".repeat(50));
        System.out.printf("  %s%n", title);
        System.out.println("═".repeat(50));
    }

    public static void printDivider() {
        System.out.println("─".repeat(50));
    }
}
