package datastructure;

import model.DeliveryOrder;
import model.FoodDonation;
import enums.OrderStatus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * DeliveryHistory — wrapper LinkedList untuk semua DeliveryOrder yang selesai (DELIVERED/CANCELLED)
 * dan FoodDonation yang WASTED.
 * Insert di head → O(1), entri terbaru selalu di posisi pertama.
 *
 * Fitur 3.2.5: menyimpan donasi WASTED hasil pembersihan startup.
 * Fitur 3.2.2: filterByShelter untuk riwayat penerimaan panti.
 */
public class DeliveryHistory {

    /** LinkedList — insert di head O(1), entri terbaru selalu pertama */
    private final LinkedList<DeliveryOrder> history;

    /** LinkedList — riwayat donasi yang di-WASTED (startup cleanup atau expired in-transit) */
    private final LinkedList<FoodDonation> wastedHistory;

    public DeliveryHistory() {
        this.history       = new LinkedList<>();
        this.wastedHistory = new LinkedList<>();
    }

    /** Insert di head, O(1) — kelebihan utama LinkedList vs ArrayList */
    public void addFirst(DeliveryOrder order) {
        history.addFirst(order);
    }

    /** Fitur 3.2.5 — Simpan donasi WASTED ke riwayat (dari startup cleanup atau in-transit) */
    public void addWasted(FoodDonation d) {
        wastedHistory.addFirst(d);
    }

    /** Order paling baru (index 0) */
    public DeliveryOrder getLatest() {
        return history.isEmpty() ? null : history.getFirst();
    }

    /** Filter berdasarkan status order */
    public List<DeliveryOrder> filterByStatus(OrderStatus status) {
        List<DeliveryOrder> result = new ArrayList<>();
        for (DeliveryOrder o : history) {
            if (o.getStatus() == status) result.add(o);
        }
        return result;
    }

    /**
     * Fitur 3.2.2 — Riwayat penerimaan untuk satu panti tertentu.
     * Dipakai shelterViewHistory di Menu.java.
     */
    public List<DeliveryOrder> filterByShelter(String shelterId) {
        List<DeliveryOrder> result = new ArrayList<>();
        for (DeliveryOrder o : history) {
            if (o.getShelter().getUserId().equals(shelterId)) result.add(o);
        }
        return result;
    }

    /** Filter berdasarkan restoran */
    public List<DeliveryOrder> filterByRestaurant(String restaurantId) {
        List<DeliveryOrder> result = new ArrayList<>();
        for (DeliveryOrder o : history) {
            for (model.Restaurant r : o.getBundle().getRestaurantList()) {
                if (r.getUserId().equals(restaurantId)) { result.add(o); break; }
            }
        }
        return result;
    }

    /** Seluruh riwayat delivery */
    public List<DeliveryOrder> getAll() {
        return new ArrayList<>(history);
    }

    /** Seluruh riwayat donasi wasted */
    public List<FoodDonation> getWastedHistory() {
        return new ArrayList<>(wastedHistory);
    }
}
