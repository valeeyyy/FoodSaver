package model;

import enums.DonationStatus;
import util.IdGenerator;
import util.SystemConfig;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class FoodDonation {

    private final String         donationId;
    private String               foodName;
    private int                  portions;
    private final LocalDateTime  cookedAt;
    private final LocalDateTime  expiredAt;
    private String               notes;
    private DonationStatus       status;
    private Restaurant           restaurant;

    public FoodDonation(String foodName, int portions,
                        LocalDateTime cookedAt, String notes,
                        Restaurant restaurant) {
        this.donationId  = IdGenerator.nextDonationId();
        this.foodName    = foodName;
        this.portions    = portions;
        this.cookedAt    = cookedAt;
        this.expiredAt   = cookedAt.plusHours(SystemConfig.MAX_FRESH_HOURS);
        this.notes       = notes;
        this.status      = DonationStatus.WAITING;
        this.restaurant  = restaurant;
    }

    /** Is this donation still fresh right now? */
    public boolean isStillFresh() {
        return LocalDateTime.now().isBefore(expiredAt);
    }

    /** Is it in the last FRESHNESS_BUFFER_MIN before expiry? */
    public boolean isInBuffer() {
        LocalDateTime bufferStart = expiredAt.minusMinutes(SystemConfig.FRESHNESS_BUFFER_MIN);
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(bufferStart) && now.isBefore(expiredAt);
    }

    /** Minutes remaining until expiry */
    public long getRemainingMinutes() {
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), expiredAt);
    }

    public void markAsMatched()  { status = DonationStatus.MATCHED; }
    public void markAsExpired()  { status = DonationStatus.EXPIRED_UNDELIVERED; }
    public void markAsWasted()   { status = DonationStatus.WASTED; }
    public void markAsDelivered(){ status = DonationStatus.DELIVERED; }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String        getDonationId() { return donationId; }
    public String        getFoodName()   { return foodName; }
    public int           getPortions()   { return portions; }
    public LocalDateTime getCookedAt()   { return cookedAt; }
    public LocalDateTime getExpiredAt()  { return expiredAt; }
    public String        getNotes()      { return notes; }
    public DonationStatus getStatus()    { return status; }
    public Restaurant    getRestaurant() { return restaurant; }

    public void setPortions(int p) { this.portions = p; }
    public void setStatus(DonationStatus s) { this.status = s; }

    @Override
    public String toString() {
        return String.format(
            "FoodDonation{id='%s', food='%s', portions=%d, expiredAt=%s, status=%s, resto='%s'}",
            donationId, foodName, portions,
            expiredAt.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
            status, restaurant.getName());
    }
}
