package model;

import enums.ActionType;
import enums.OrderStatus;
import util.IdGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DeliveryOrder — satu pesanan pengiriman yang dibuat oleh MatchingEngine.
 * Status bergerak berurutan: WAITING_PICKUP → PICKED_UP → IN_TRANSIT → DELIVERED / CANCELLED.
 * Tidak bisa melompat tahap.
 */
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
        this.orderId          = IdGenerator.nextOrderId();
        this.bundle           = bundle;
        this.shelter          = shelter;
        this.courierId        = courierId;
        this.status           = OrderStatus.WAITING_PICKUP;
        this.estimatedArrival = estimatedArrival;
        this.portionSurplus   = portionSurplus;
        this.rating           = 0;
        this.receiptNotes     = "";
        this.createdAt        = LocalDateTime.now();
        this.statusTimeline   = new ArrayList<>();
        this.statusTimeline.add("[" + createdAt.format(FMT) + "] WAITING_PICKUP");
    }

    /** Memajukan status ke tahap berikutnya secara berurutan — tidak bisa dilompat */
    public void advanceStatus() {
        switch (status) {
            case WAITING_PICKUP -> status = OrderStatus.PICKED_UP;
            case PICKED_UP      -> status = OrderStatus.IN_TRANSIT;
            case IN_TRANSIT     -> status = OrderStatus.DELIVERED;
            default -> System.out.println("[!] Status sudah final: " + status);
        }
        statusTimeline.add("[" + LocalDateTime.now().format(FMT) + "] " + status);
    }

    /** Panti mengkonfirmasi penerimaan dan memberikan rating */
    public void confirmDelivery(int rating, String notes) {
        this.status       = OrderStatus.DELIVERED;
        this.rating       = rating;
        this.receiptNotes = notes;
        statusTimeline.add("[" + LocalDateTime.now().format(FMT) + "] DELIVERED");
        shelter.addPortionsToday(bundle.getTotalPortions() - portionSurplus);
    }

    /** Salah satu donasi dalam bundle expired saat IN_TRANSIT → catat WASTED */
    public void markPartialWasted(FoodDonation wastedDonation) {
        wastedDonation.markAsWasted();
        portionSurplus += wastedDonation.getPortions();
        System.out.printf("[!] Donasi %s WASTED saat pengiriman — porsi dikurangi %d.%n",
                wastedDonation.getDonationId(), wastedDonation.getPortions());
    }

    public List<String> getStatusTimeline() { return statusTimeline; }

    public String    getOrderId()         { return orderId; }
    public DonationBundle getBundle()     { return bundle; }
    public Shelter   getShelter()         { return shelter; }
    public String    getCourierId()       { return courierId; }
    public OrderStatus getStatus()        { return status; }
    public long      getEstimatedArrival(){ return estimatedArrival; }
    public int       getPortionSurplus()  { return portionSurplus; }
    public int       getRating()          { return rating; }
    public String    getReceiptNotes()    { return receiptNotes; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
}
