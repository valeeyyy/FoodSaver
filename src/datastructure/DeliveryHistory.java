package datastructure;

import java.util.ArrayList;
import model.DeliveryOrder;

public class DeliveryHistory {

    private ArrayList<DeliveryOrder> orderList;

    public DeliveryHistory() {
        this.orderList = new ArrayList<>();
    }

    public void addOrder(DeliveryOrder order) {
        orderList.add(order);
        System.out.println("Order " + order.getOrderId() + " berhasil dicatat di sejarah.");
    }

    public ArrayList<DeliveryOrder> getAllOrders() {
        return orderList;
    }
}