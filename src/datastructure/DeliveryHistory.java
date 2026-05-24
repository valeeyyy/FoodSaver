package datastructure;

import model.DeliveryOrder;
import model.FoodDonation;
import enums.OrderStatus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DeliveryHistory {

    private final LinkedList<DeliveryOrder> history;
    private final LinkedList<FoodDonation> wastedHistory;
    private final LinkedList<FoodDonation> expiredHistory; // red alert sesi ini

    public DeliveryHistory() {
        this.history = new LinkedList<>();
        this.wastedHistory = new LinkedList<>();
        this.expiredHistory = new LinkedList<>();
    }

    public void addFirst(DeliveryOrder order) {
        history.addFirst(order);
    }

    public void addWasted(FoodDonation d) {
        wastedHistory.addFirst(d);
    }

    public void addExpired(FoodDonation d) {
        expiredHistory.addFirst(d);
    }

    public DeliveryOrder getLatest() {
        return history.isEmpty() ? null : history.getFirst();
    }

    public List<DeliveryOrder> filterByStatus(OrderStatus status) {
        List<DeliveryOrder> result = new ArrayList<>();
        for (DeliveryOrder o : history) {
            if (o.getStatus() == status)
                result.add(o);
        }
        return result;
    }

    public List<DeliveryOrder> filterByShelter(String shelterId) {
        List<DeliveryOrder> result = new ArrayList<>();
        for (DeliveryOrder o : history) {
            if (o.getShelter().getUserId().equals(shelterId))
                result.add(o);
        }
        return result;
    }

    public List<DeliveryOrder> filterByRestaurant(String restaurantId) {
        List<DeliveryOrder> result = new ArrayList<>();
        for (DeliveryOrder o : history) {
            for (model.Restaurant r : o.getBundle().getRestaurantList()) {
                if (r.getUserId().equals(restaurantId)) {
                    result.add(o);
                    break;
                }
            }
        }
        return result;
    }

    public List<DeliveryOrder> getAll() {
        return new ArrayList<>(history);
    }

    public List<FoodDonation> getWastedHistory() {
        return new ArrayList<>(wastedHistory);
    }

    public List<FoodDonation> getExpiredHistory() {
        return new ArrayList<>(expiredHistory);
    }
}
