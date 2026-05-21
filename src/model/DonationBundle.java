package model;

import util.IdGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DonationBundle {

    private String bundleId;
    private List<FoodDonation> donations;
    private LocalDateTime createdAt;

    public DonationBundle() {
        this.bundleId = IdGenerator.nextBundleId();
        this.donations = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    public void addDonation(FoodDonation donation) {
        donations.add(donation);
    }

    public int getTotalPortions() {
        int total = 0;

        for (FoodDonation donation : donations) {
            total += donation.getPortions();
        }

        return total;
    }

    public LocalDateTime getEarliestExpiry() {

        if (donations.isEmpty()) {
            return LocalDateTime.MAX;
        }

        LocalDateTime earliest = donations.get(0).getExpiredAt();

        for (FoodDonation donation : donations) {
            LocalDateTime expired = donation.getExpiredAt();

            if (expired.isBefore(earliest)) {
                earliest = expired;
            }
        }

        return earliest;
    }

    public List<Restaurant> getRestaurantList() {
        List<Restaurant> restaurants = new ArrayList<>();

        for (FoodDonation donation : donations) {
            Restaurant r = donation.getRestaurant();

            if (!restaurants.contains(r)) {
                restaurants.add(r);
            }
        }

        return restaurants;
    }

    public boolean isStillValid() {
        for (FoodDonation donation : donations) {
            if (!donation.isStillFresh()) {
                return false;
            }
        }
        return true;
    }

    public String getBundleId() {
        return bundleId;
    }

    public List<FoodDonation> getDonations() {
        return donations;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}