package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import enums.ActionType;
import enums.ShelterType;
import model.Restaurant;
import model.Shelter;
import model.User;
import datastructure.AuditLog;

public class Menu {

    public static final List<User>     allUsers = new ArrayList<>();
    public static final AuditLog       auditLog = new AuditLog();

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        showMainMenu();
    }

    public static void showMainMenu() {
        while (true) {
            printHeader("FOODSAVER");
            System.out.println("  [1] Login");
            System.out.println("  [2] Registrasi");
            System.out.println("  [0] Keluar");
            System.out.print("Pilihan : ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> showLogin();
                case "2" -> showRegistration();
                case "0" -> {
                    System.out.println("Sampai jumpa!");
                    return;
                }
                default  -> System.out.println("[!] Pilihan tidak valid.\n");
            }
        }
    }

    private static void showLogin() {
        printHeader("LOGIN");
        String username = readNonEmpty("Username : ");
        String password = readNonEmpty("Password : ");

        User found = allUsers.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (found == null) {
            System.out.println("[✗] Username atau password salah.\n");
            return;
        }
        if (!found.isVerified()) {
            System.out.println("[!] Akun Anda belum diverifikasi oleh admin.\n");
            return;
        }

        System.out.println("[✓] Login berhasil. Selamat datang, " + found.getUsername() + "!\n");
        auditLog.log(username, ActionType.LOGIN, found.getUserId(), "Login successful");
    }

    public static void showRegistration() {
        printHeader("REGISTRASI AKUN BARU");
        System.out.println("  [1] Daftar sebagai Restoran");
        System.out.println("  [2] Daftar sebagai Panti");
        System.out.println("  [0] Kembali");
        System.out.print("Pilihan : ");
        String choice = sc.nextLine().trim();
        switch (choice) {
            case "1" -> registerRestaurant();
            case "2" -> registerShelter();
            case "0" -> {return;}
            default  -> System.out.println("[!] Pilihan tidak valid.\n");
        }
    }

    private static void registerRestaurant() {
        printHeader("REGISTRASI RESTORAN");

        String name     = readNonEmpty("Nama restoran     : ");
        String owner    = readNonEmpty("Nama pemilik      : ");
        String address  = readNonEmpty("Alamat            : ");
        double lat      = readDoubleInRange("Koordinat (lat)   : ", -90.0, 90.0);
        double lon      = readDoubleInRange("Koordinat (lon)   : ", -180.0, 180.0);
        String category = readNonEmpty("Jenis makanan     : ");
        String phone    = readPhoneNumber("No. HP            : ");
        String username = readUniqueUsername("Username          : ");
        String password = readPassword("Password          : ");

        Restaurant r = new Restaurant(name, owner, username, password, phone, address, lat, lon, category);
        allUsers.add(r);
        auditLog.log(username, ActionType.REGISTER, r.getUserId(), "Registered restaurant: " + name);

        System.out.println("\n[✓] Registrasi berhasil. Akun Anda sedang menunggu verifikasi admin.\n");
    }

    private static void registerShelter() {
        printHeader("REGISTRASI PANTI");

        String name      = readNonEmpty("Nama panti        : ");
        String manager   = readNonEmpty("Nama pengurus     : ");
        String address   = readNonEmpty("Alamat            : ");
        double lat       = readDoubleInRange("Koordinat (lat)   : ", -90.0, 90.0);
        double lon       = readDoubleInRange("Koordinat (lon)   : ", -180.0, 180.0);
        int    residents = readPositiveInt("Jumlah penghuni   : ");

        System.out.println("Jenis panti       : [1] Anak Yatim  [2] Lansia  [3] Disabilitas");
        int typeChoice = readIntInRange("Pilihan           : ", 1, 3);
        ShelterType type = switch (typeChoice) {
            case 2  -> ShelterType.LANSIA;
            case 3  -> ShelterType.DISABILITAS;
            default -> ShelterType.ANAK_YATIM;
        };

        String phone    = readPhoneNumber("No. HP            : ");
        String username = readUniqueUsername("Username          : ");
        String password = readPassword("Password          : ");

        Shelter s = new Shelter(name, manager, username, password, phone, address, lat, lon, residents, type);
        allUsers.add(s);
        auditLog.log(username, ActionType.REGISTER, s.getUserId(), "Registered shelter: " + name);

        System.out.println("\n[✓] Registrasi berhasil. Akun Anda sedang menunggu verifikasi admin.\n");
    }

    public static void printHeader(String title) {
        System.out.println("\n========================================");
        System.out.println("  " + title);
        System.out.println("========================================");
    }

    public static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String val = sc.nextLine().trim();
            if (!val.isEmpty()) return val;
            System.out.println("[!] Input tidak boleh kosong.");
        }
    }

    public static double readDoubleInRange(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                double val = Double.parseDouble(sc.nextLine().trim());
                if (val >= min && val <= max) return val;
                System.out.printf("[!] Masukkan angka antara %.1f dan %.1f.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("[!] Input harus berupa angka.");
            }
        }
    }

    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                if (val >= min && val <= max) return val;
                System.out.printf("[!] Masukkan angka antara %d dan %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("[!] Input harus berupa angka bulat.");
            }
        }
    }

    public static int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                if (val > 0) return val;
                System.out.println("[!] Angka harus lebih dari 0.");
            } catch (NumberFormatException e) {
                System.out.println("[!] Input harus berupa angka bulat positif.");
            }
        }
    }

    public static String readPhoneNumber(String prompt) {
        while (true) {
            System.out.print(prompt);
            String val = sc.nextLine().trim();
            if (val.matches("\\d{10,15}")) return val;
            System.out.println("[!] Nomor HP tidak valid (10–15 digit angka).");
        }
    }

    public static String readUsername(String prompt) {
        while (true) {
            System.out.print(prompt);
            String val = sc.nextLine().trim();
            if (val.matches("[a-zA-Z0-9_]{4,}")) return val;
            System.out.println("[!] Username minimal 4 karakter (huruf, angka, underscore).");
        }
    }

    public static String readUniqueUsername(String prompt) {
        while (true) {
            String username = readUsername(prompt);
            if (allUsers.stream().anyMatch(x -> x.getUsername().equals(username))) {
                System.out.println("[✗] Username \"" + username + "\" sudah digunakan.");
            } else {
                return username;
            }
        }
    }

    public static String readPassword(String prompt) {
        while (true) {
            System.out.print(prompt);
            String val = sc.nextLine().trim();
            if (val.length() >= 6) return val;
            System.out.println("[!] Password minimal 6 karakter.");
        }
    }
}