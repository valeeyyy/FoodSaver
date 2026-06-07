package model;

import enums.DonationStatus;
import java.time.LocalDateTime;
import util.IdGenerator;
import util.SystemConfig;

public class FoodDonation {

    private final String donationId;
    private String foodName;
    private int portions;
    private final LocalDateTime cookedAt;
    private final LocalDateTime expiredAt;
    private String notes;
    private DonationStatus status;
    private Restaurant restaurant;
    private boolean yellowAlertFired = false;

    public FoodDonation(String foodName, int portions,
            LocalDateTime cookedAt, String notes,
            Restaurant restaurant) {
        this.donationId = IdGenerator.nextDonationId();
        this.foodName = foodName;
        this.portions = portions;
        this.cookedAt = cookedAt;
        this.expiredAt = cookedAt.plusHours(SystemConfig.MAX_FRESH_HOURS);
        this.notes = notes;
        this.status = DonationStatus.WAITING;
        this.restaurant = restaurant;
    }

    public boolean isStillFresh() {
        return LocalDateTime.now().isBefore(expiredAt);
    }

    public boolean isInBuffer() {
        LocalDateTime bufferStart = expiredAt.minusMinutes(SystemConfig.FRESHNESS_BUFFER_MIN);
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(bufferStart) && now.isBefore(expiredAt);
    }

    public long getRemainingMinutes() {
        LocalDateTime now = LocalDateTime.now();
        java.time.Duration diff = java.time.Duration.between(now, expiredAt);
        return diff.getSeconds() / 60;
    }

    public void markAsMatched() {
        status = DonationStatus.MATCHED;
    }

    public void markAsExpired() {
        status = DonationStatus.EXPIRED_UNDELIVERED;
    }

    public void markAsWasted() {
        status = DonationStatus.WASTED;
    }

    public void markAsDelivered() {
        status = DonationStatus.DELIVERED;
    }

    public void markAsCancelled() {
        status = DonationStatus.CANCELLED;
    }

    public String getDonationId() {
        return donationId;
    }

    public String getFoodName() {
        return foodName;
    }

    public int getPortions() {
        return portions;
    }

    public LocalDateTime getCookedAt() {
        return cookedAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public String getNotes() {
        return notes;
    }

    public DonationStatus getStatus() {
        return status;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public boolean isYellowAlertFired() {
        return yellowAlertFired;
    }

    public void setYellowAlertFired(boolean v) {
        yellowAlertFired = v;
    }

    public void setPortions(int p) {
        this.portions = p;
    }

    public void setStatus(DonationStatus s) {
        this.status = s;
    }

    @Override
    public String toString() {

        String restoName = "-";

        if (restaurant != null) {
            restoName = restaurant.getName();
        }

        return "FoodDonation [id='" + donationId +
                "', food='" + foodName +
                "', portions=" + portions +
                ", expiredAt=" + expiredAt +
                ", status=" + status +
                ", resto='" + restoName +
                "']";
    }
}
