package datastructure;

import java.time.LocalDateTime;
import java.util.*;

import model.FoodDonation;

public class FoodExpiryTree {
    private final TreeMap<LocalDateTime, List<FoodDonation>> tree;

    public FoodExpiryTree() {
        this.tree = new TreeMap<>();
    }

    public void remove(FoodDonation d) {
        List<FoodDonation> bucket = tree.get(d.getExpiredAt());
        if (bucket != null) {
            bucket.remove(d);
            if (bucket.isEmpty())
                tree.remove(d.getExpiredAt());
        }
    }

    public int size() {
        return tree.values().stream().mapToInt(List::size).sum();
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
