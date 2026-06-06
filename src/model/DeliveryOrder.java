package model;

import enums.DonationStatus;
import enums.OrderStatus;
import util.IdGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DeliveryOrder {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final String orderId;
    private final DonationBundle bundle;
    private final Shelter shelter;
    private String courierId;
    private OrderStatus status;
    private final long estimatedArrival;
    private int portionSurplus;
    private int rating;
    private String receiptNotes;
    private final LocalDateTime createdAt;
    private final List<String> statusTimeline;

    public DeliveryOrder(DonationBundle bundle, Shelter shelter,
            String courierId, long estimatedArrival, int portionSurplus) {
        this.orderId = IdGenerator.nextOrderId();
        this.bundle = bundle;
        this.shelter = shelter;
        this.courierId = courierId;
        this.status = OrderStatus.WAITING_PICKUP;
        this.estimatedArrival = estimatedArrival;
        this.portionSurplus = portionSurplus;
        this.rating = 0;
        this.receiptNotes = "";
        this.createdAt = LocalDateTime.now();
        this.statusTimeline = new ArrayList<>();
        this.statusTimeline.add("[" + createdAt.format(FMT) + "] WAITING_PICKUP");
    }

    public void setStatusSilent(OrderStatus target) {
        this.status = target;
        this.statusTimeline.add(createdAt.format(FMT) + " → " + target);
    }

    public void advanceStatus() {
        switch (status) {
            case WAITING_PICKUP -> {
                status = OrderStatus.PICKED_UP;
                statusTimeline.add(LocalDateTime.now().format(FMT) + " → PICKED_UP");
                System.out.println("[✓] Status diperbarui: PICKED_UP");
            }
            case PICKED_UP -> {
                status = OrderStatus.IN_TRANSIT;
                statusTimeline.add(LocalDateTime.now().format(FMT) + " → IN_TRANSIT");
                System.out.println("[✓] Status diperbarui: IN_TRANSIT");
                checkBundleFreshness();
            }
            default -> System.out.println("[!] Status sudah pada tahap akhir atau menunggu konfirmasi panti.");
        }
    }

    public void confirmDelivery(int rating, String notes) {
        this.rating = rating;
        this.receiptNotes = notes;
        this.status = OrderStatus.DELIVERED;
        statusTimeline.add(LocalDateTime.now().format(FMT) + " → DELIVERED (confirmed)");

        for (FoodDonation d : bundle.getDonations()) {
            if (d.getStatus() != DonationStatus.WASTED) {
                d.markAsDelivered();
            }
        }
    }

    public void markPartialWasted(FoodDonation wastedDonation) {
        wastedDonation.markAsWasted();
        portionSurplus = Math.max(0, portionSurplus - wastedDonation.getPortions());
        System.out.printf("[!] Donasi %s EXPIRED saat IN_TRANSIT — ditandai WASTED, porsi dikurangi %d.%n",
                wastedDonation.getDonationId(), wastedDonation.getPortions());
    }

    private void checkBundleFreshness() {
        for (FoodDonation d : bundle.getDonations()) {
            if (!d.isStillFresh()) {
                markPartialWasted(d);
            }
        }
    }

    public List<String> getStatusTimeline() {
        return statusTimeline;
    }

    public String getOrderId() {
        return orderId;
    }

    public DonationBundle getBundle() {
        return bundle;
    }

    public Shelter getShelter() {
        return shelter;
    }

    public String getCourierId() {
        return courierId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public long getEstimatedArrival() {
        return estimatedArrival;
    }

    public int getPortionSurplus() {
        return portionSurplus;
    }

    public int getRating() {
        return rating;
    }

    public String getReceiptNotes() {
        return receiptNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
