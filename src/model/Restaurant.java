package model;

import enums.AccountStatus;
import enums.DonationStatus;

import java.util.*;

public class Restaurant extends User {

    private String name;
    private String ownerName;
    private double lat;
    private double lon;
    private String foodCategory;
    private List<FoodDonation> donations;

    public Restaurant(String name, String ownerName,
            String username, String password,
            String phone, String address,
            double lat, double lon, String foodCategory) {
        super(username, password, phone, address);
        this.name = name;
        this.ownerName = ownerName;
        this.lat = lat;
        this.lon = lon;
        this.foodCategory = foodCategory;
        this.donations = new LinkedList<>();
    }

    public void addDonation(FoodDonation d) {
        donations.add(d);
    }

    public void postDonation(FoodDonation d) {
        if (accountStatus != AccountStatus.APPROVED) {
            System.out.println("[✗] Akun belum diverifikasi. Tidak dapat memposting donasi.");
            return;
        }
        donations.add(d);
        System.out.println("[✓] Donasi berhasil diposting. ID: " + d.getDonationId());
    }

    public void cancelDonation(String donationId) {
        for (FoodDonation d : donations) {
            if (d.getDonationId().equals(donationId)) {
                if (d.getStatus() == DonationStatus.MATCHED) {
                    System.out.println("[✗] Donasi sudah dicocokkan. Tidak dapat dibatalkan.");
                    return;
                }
                d.markAsExpired();
                System.out.println("[✓] Donasi " + donationId + " berhasil dibatalkan.");
                return;
            }
        }
        System.out.println("[✗] Donasi dengan ID " + donationId + " tidak ditemukan.");
    }

    public void editPortions(String donationId, int newPortions) {
        for (FoodDonation d : donations) {
            if (d.getDonationId().equals(donationId)) {
                if (d.getStatus() != DonationStatus.WAITING) {
                    System.out.println("[✗] Donasi tidak bisa diubah (status: " + d.getStatus() + ").");
                    return;
                }
                d.setPortions(newPortions);
                System.out.println("[✓] Jumlah porsi diperbarui menjadi " + newPortions + ".");
                return;
            }
        }
        System.out.println("[✗] Donasi tidak ditemukan.");
    }

    public List<FoodDonation> viewDonationHistory() {
        return donations;
    }

    public String getName() {
        return name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getFoodCategory() {
        return foodCategory;
    }

    public List<FoodDonation> getDonations() {
        return donations;
    }

    public void setName(String name)             { this.name = name; }
    public void setOwnerName(String ownerName)   { this.ownerName = ownerName; }
    public void setLat(double lat)               { this.lat = lat; }
    public void setLon(double lon)               { this.lon = lon; }
    public void setFoodCategory(String cat)      { this.foodCategory = cat; }

    @Override
    public String toString() {
        return String.format("Restaurant{id='%s', name='%s', owner='%s', status=%s}",
                userId, name, ownerName, accountStatus);
    }
}
