package model;

import util.IdGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DonationBundle — gabungan satu atau lebih FoodDonation menjadi satu paket pengiriman.
 * Dibuat oleh MatchingEngine ketika satu donasi saja tidak cukup memenuhi kebutuhan porsi panti.
 */
public class DonationBundle {

    private final String bundleId;
    private final List<FoodDonation> donations;
    private final LocalDateTime createdAt;

    public DonationBundle() {
        this.bundleId  = IdGenerator.nextBundleId();
        this.donations = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    public void addDonation(FoodDonation d) {
        donations.add(d);
    }

    /** Total porsi dari semua donasi dalam bundle — dipakai Filter 1 MatchingEngine */
    public int getTotalPortions() {
        int total = 0;
        for (FoodDonation d : donations) total += d.getPortions();
        return total;
    }

    /** Expiry paling awal dalam bundle — "expiry kritis" untuk cek freshness */
    public LocalDateTime getEarliestExpiry() {
        LocalDateTime earliest = null;
        for (FoodDonation d : donations) {
            if (earliest == null || d.getExpiredAt().isBefore(earliest))
                earliest = d.getExpiredAt();
        }
        return earliest;
    }

    /** Daftar restoran pendonor — dipakai GeoUtils untuk estimasi rute */
    public List<Restaurant> getRestaurantList() {
        List<Restaurant> list = new ArrayList<>();
        for (FoodDonation d : donations) list.add(d.getRestaurant());
        return list;
    }

    /** Cek apakah semua donasi dalam bundle masih segar */
    public boolean isStillValid() {
        for (FoodDonation d : donations) {
            if (!d.isStillFresh()) return false;
        }
        return true;
    }

    public String getBundleId()          { return bundleId; }
    public List<FoodDonation> getDonations() { return donations; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
}
