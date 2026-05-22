package datastructure;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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

    // Method insert yang sebelumnya hilang
    public void insert(FoodDonation d) {
        tree.computeIfAbsent(d.getExpiredAt(), k -> new ArrayList<>()).add(d);
    }

    public void remove(FoodDonation d) {
        List<FoodDonation> bucket = tree.get(d.getExpiredAt());
        if (bucket != null) {
            bucket.remove(d);
            if (bucket.isEmpty()) tree.remove(d.getExpiredAt());
        }
    }
}