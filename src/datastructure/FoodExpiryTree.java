package datastructure;

import java.util.Comparator;
import java.util.PriorityQueue;
import model.FoodDonation;

public class FoodExpiryTree {

    private PriorityQueue<FoodDonation> expiryQueue;

    public FoodExpiryTree() {

        expiryQueue = new PriorityQueue<>(new Comparator<FoodDonation>() {
            @Override
            public int compare(FoodDonation d1, FoodDonation d2) {
                return d1.getExpiredAt().compareTo(d2.getExpiredAt());
            }
        });
    }

    public void addDonation(FoodDonation donation) {
        expiryQueue.add(donation);
    }

    public FoodDonation getEarliestExpiringDonation() {
        return expiryQueue.poll(); 
    }
}
