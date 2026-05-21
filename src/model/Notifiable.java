package model;

public interface Notifiable {
    void sendAlert(String msg);
    void onDonationExpired(FoodDonation d);
    void onMatchFound(DeliveryOrder order);
}
