package model;

import engine.MatchOption;
import util.IdGenerator;

public class DeliveryOrder {
    private String orderId;
    private MatchOption matchOption;
    private String courierName;
    private long estimatedArrivalMs;

    public DeliveryOrder(MatchOption matchOption) {
        this.orderId = "ORD-" + System.currentTimeMillis(); 
        this.matchOption = matchOption;
    }

    public void assignCourier(String name) {
        this.courierName = name;
    }

    public void setEstimatedArrivalMs(long timeMs) {
        this.estimatedArrivalMs = timeMs;
    }

    public String getOrderId() {
        return orderId;
    }

    public MatchOption getMatchOption() {
        return matchOption;
    }
}