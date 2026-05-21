package engine;

import model.DonationBundle;
import model.Shelter;

public class MatchOption {

    private DonationBundle bundle;
    private Shelter shelter;
    private double totalRouteKm;
    private long arrivalMs;
    private int portionSurplus;

    public MatchOption(DonationBundle bundle, Shelter shelter, double totalRouteKm, long arrivalMs) {
        this.bundle = bundle;
        this.shelter = shelter;
        this.totalRouteKm = totalRouteKm;
        this.arrivalMs = arrivalMs;
        this.portionSurplus = bundle.getTotalPortions() - shelter.getRemainingNeed();
    }

    public double getTotalRouteKm() {
        return totalRouteKm;
    }

    public int getResidentsServed() {
        return shelter.getResidents();
    }

    public java.time.LocalDateTime getEarliestExpiry() {
        return bundle.getEarliestExpiry();
    }

    public int getPortionSurplus() {
        return portionSurplus;
    }

    public DonationBundle getBundle() {
        return bundle;
    }

    public Shelter getShelter() {
        return shelter;
    }

    public long getArrivalMs() {
        return arrivalMs;
    }

    @Override
    public String toString() {
        return "MatchOption { " + "bundleId = " + bundle.getBundleId() + ", shelter = '" + shelter.getName() + 
        "'" + ", routeKm = " + totalRouteKm + ", surplus = " + portionSurplus + " }";
    }

}