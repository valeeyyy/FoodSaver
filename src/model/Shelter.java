package model;

import enums.ShelterType;

import java.util.LinkedList;
import java.util.List;

public class Shelter extends User {

    private String name;
    private String managerName;
    private double lat;
    private double lon;
    private int residents;
    private ShelterType shelterType;
    private int portionsToday;

    private final LinkedList<DeliveryOrder> receiptHistory;

    public Shelter(String name, String managerName,
            String username, String password,
            String phone, String address,
            double lat, double lon,
            int residents, ShelterType shelterType) {
        super(username, password, phone, address);
        this.name = name;
        this.managerName = managerName;
        this.lat = lat;
        this.lon = lon;
        this.residents = residents;
        this.shelterType = shelterType;
        this.portionsToday = 0;
        this.receiptHistory = new LinkedList<>();
    }

    public void confirmReceipt(DeliveryOrder order, int rating, String notes) {
        order.confirmDelivery(rating, notes);
        int totalPortions    = order.getBundle().getTotalPortions();
        int surplus          = order.getPortionSurplus();
        int portionsReceived = totalPortions - surplus;
        addPortionsToday(portionsReceived);
        receiptHistory.addFirst(order);
    }

    public List<DeliveryOrder> viewReceiptHistory() {
        return receiptHistory;
    }

    public void updateResidents(int count) {
        this.residents = count;
        System.out.println("[✓] Jumlah penghuni diperbarui: " + count + " orang.");
    }

    public boolean canReceiveMore() {
        return portionsToday < residents;
    }

    public int getRemainingNeed() {
        return Math.max(0, residents - portionsToday);
    }

    public void resetDailyPortions() {
        portionsToday = 0;
    }

    public void addPortionsToday(int p) {
        portionsToday += p;
    }

    public String getName() {
        return name;
    }

    public String getManagerName() {
        return managerName;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public int getResidents() {
        return residents;
    }

    public ShelterType getShelterType() {
        return shelterType;
    }

    public int getPortionsToday() {
        return portionsToday;
    }

    public void setName(String name)             { this.name = name; }
    public void setManagerName(String mgr)       { this.managerName = mgr; }
    public void setLat(double lat)               { this.lat = lat; }
    public void setLon(double lon)               { this.lon = lon; }
    public void setResidents(int residents)      { this.residents = residents; }
    public void setShelterType(ShelterType type) { this.shelterType = type; }

    @Override
    public String toString() {
        return String.format("Shelter{id='%s', name='%s', residents=%d, need=%d, status=%s}",
                userId, name, residents, getRemainingNeed(), accountStatus);
    }
}
