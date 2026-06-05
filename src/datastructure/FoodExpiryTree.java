package datastructure;

import model.FoodDonation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FoodExpiryTree {

    private final TreeMap<LocalDateTime, List<FoodDonation>> tree;

    public FoodExpiryTree() {
        this.tree = new TreeMap<>();
    }

    public void insert(FoodDonation d) {
        tree.computeIfAbsent(d.getExpiredAt(), k -> new ArrayList<>()).add(d);
    }

    public List<FoodDonation> getByPriority() {
        List<FoodDonation> result = new ArrayList<>();
        for (Map.Entry<LocalDateTime, List<FoodDonation>> entry : tree.entrySet()) {
            result.addAll(entry.getValue());
        }
        return result;
    }

    public FoodDonation peekEarliest() {
        if (tree.isEmpty())
            return null;
        Map.Entry<LocalDateTime, List<FoodDonation>> first = tree.firstEntry();
        List<FoodDonation> list = first.getValue();
        return list.isEmpty() ? null : list.get(0);
    }

    public void remove(FoodDonation d) {
        List<FoodDonation> bucket = tree.get(d.getExpiredAt());
        if (bucket != null) {
            bucket.remove(d);
            if (bucket.isEmpty())
                tree.remove(d.getExpiredAt());
        }
    }

    public int purgeExpired() {
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> expiredKeys = new ArrayList<>();
        int count = 0;
        for (Map.Entry<LocalDateTime, List<FoodDonation>> entry : tree.entrySet()) {
            if (entry.getKey().isBefore(now)) {
                count += entry.getValue().size();
                expiredKeys.add(entry.getKey());
            } else
                break;
        }
        for (LocalDateTime k : expiredKeys)
            tree.remove(k);
        return count;
    }

    public int size() {
        return tree.values().stream().mapToInt(List::size).sum();
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
