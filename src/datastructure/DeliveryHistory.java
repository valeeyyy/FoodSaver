package datastructure;

import model.FoodDonation;

import java.util.*;

public class DeliveryHistory {

    private final LinkedList<FoodDonation> history;

    public DeliveryHistory() {
        this.history = new LinkedList<>();
    }

    public void addFirst(FoodDonation d) {
        history.addFirst(d);
    }

    public List<FoodDonation> getAll() {
        return new ArrayList<>(history);
    }

    public int size() { 
        return history.size(); 
    }
    
    public boolean isEmpty() { 
        return history.isEmpty(); 
    }
}
