package datastructure;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import model.FoodDonation;

public class FoodExpiryTree {
    private final TreeMap<LocalDateTime, List<FoodDonation>> tree;

    public FoodExpiryTree() {
        this.tree = new TreeMap<>();
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