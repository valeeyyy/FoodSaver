package datastructure;

import java.util.ArrayList;
import java.util.List;

import model.DeliveryOrder;
import enums.OrderStatus;

public class DeliveryHistory {

    private ArrayList<DeliveryOrder> orderList;

    public DeliveryHistory() {
        this.orderList = new ArrayList<>();
    }

    public void addOrder(DeliveryOrder order) {
        orderList.add(order);
        System.out.println("Order " + order.getOrderId() + " berhasil dicatat di History.");
    }

    public ArrayList<DeliveryOrder> getAllOrders() {
        return orderList;
    }

    public List<DeliveryOrder> filterByStatus(OrderStatus status) {
        List<DeliveryOrder> result = new ArrayList<>();

        for (DeliveryOrder order : orderList) {
            if (order.getStatus() == status) {
                result.add(order);
            }
        }
        return result;
    }
}