package engine;

import datastructure.AuditLog;
import datastructure.DeliveryHistory;
import datastructure.DonationPool;
import datastructure.FoodExpiryTree;
import datastructure.ShelterRegistry;
import model.*;
import enums.ActionType;
import util.GeoUtils;
import util.SystemConfig;

import java.util.ArrayList;
import java.util.List;

public class MatchingEngine implements Notifiable {

    private final DonationPool pool;
    private final ShelterRegistry registry;
    private final FoodExpiryTree expiryTree;
    private final DeliveryHistory history;
    private final AuditLog auditLog;

    public MatchingEngine(DonationPool pool, ShelterRegistry registry,
            FoodExpiryTree expiryTree, DeliveryHistory history,
            AuditLog auditLog) {
        this.pool = pool;
        this.registry = registry;
        this.expiryTree = expiryTree;
        this.history = history;
        this.auditLog = auditLog;
    }

    public void run() {
        runWithRadius(SystemConfig.MAX_RADIUS_KM);
    }

    public void runWithExpandedRadius() {
        System.out.println("[MatchingEngine] Menjalankan matching dengan radius diperluas: "
                + SystemConfig.EXPANDED_RADIUS_KM + " km");
        runWithRadius(SystemConfig.EXPANDED_RADIUS_KM);
    }

    private void runWithRadius(double radiusKm) {
        System.out.println("\n=== MATCHING ENGINE (radius=" + radiusKm + "km) ===");

        List<Shelter> eligibleShelters = registry.findAllEligible();
        if (eligibleShelters.isEmpty()) {
            System.out.println("[!] Tidak ada panti yang membutuhkan donasi saat ini.");
            return;
        }

        List<FoodDonation> activeDonations = expiryTree.getByPriority();
        if (activeDonations.isEmpty()) {
            System.out.println("[!] Tidak ada donasi aktif di antrian.");
            return;
        }

        List<DonationBundle> bundles = generateCombinations(activeDonations);

        List<MatchOption> validOptions = new ArrayList<>();
        for (DonationBundle bundle : bundles) {
            for (Shelter shelter : eligibleShelters) {
                if (applyFilters(bundle, shelter, radiusKm)) {
                    double routeKm = GeoUtils.getTotalRouteKm(
                            bundle.getRestaurantList(), shelter);
                    long arrivalMs = GeoUtils.estimateArrivalMs(
                            bundle.getRestaurantList(), shelter);
                    validOptions.add(new MatchOption(bundle, shelter, routeKm, arrivalMs));
                }
            }
        }

        if (validOptions.isEmpty()) {
            System.out.println("[✗] Tidak ada kombinasi donasi-panti yang valid saat ini.");
            return;
        }

        MatchOption winner = scoreAndSelect(validOptions);
        System.out.println("[✓] Match ditemukan: " + winner);

        DeliveryOrder order = createDeliveryOrder(winner);
        history.addFirst(order);

        for (FoodDonation d : winner.getBundle().getDonations()) {
            d.markAsMatched();
            pool.remove(d);
            expiryTree.remove(d);
        }

        int portionsDelivered = winner.getBundle().getTotalPortions() - winner.getPortionSurplus();
        winner.getShelter().addPortionsToday(portionsDelivered);

        auditLog.log("SYSTEM", ActionType.MATCH,
                winner.getBundle().getBundleId(),
                "Matched to " + winner.getShelter().getName());

        onMatchFound(order);

        if (!pool.isEmpty()) {
            System.out.println("[MatchingEngine] Mencoba mencocokkan donasi tersisa...");
            runWithRadius(radiusKm);
        }
    }

    List<DonationBundle> generateCombinations(List<FoodDonation> donations) {
        List<DonationBundle> result = new ArrayList<>();
        int n = Math.min(donations.size(), 6);
        for (int mask = 1; mask < (1 << n); mask++) {
            DonationBundle bundle = new DonationBundle();
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    bundle.addDonation(donations.get(i));
                }
            }
            result.add(bundle);
        }
        return result;
    }

    boolean applyFilters(DonationBundle bundle, Shelter shelter, double radiusKm) {
        return filterPortions(bundle, shelter)
                && filterDistance(bundle, shelter, radiusKm)
                && filterFreshness(bundle, GeoUtils.estimateArrivalMs(
                        bundle.getRestaurantList(), shelter));
    }

    boolean filterPortions(DonationBundle bundle, Shelter shelter) {
        return bundle.getTotalPortions() >= shelter.getRemainingNeed();
    }

    boolean filterDistance(DonationBundle bundle, Shelter shelter, double radiusKm) {
        for (Restaurant r : bundle.getRestaurantList()) {
            double km = GeoUtils.euclideanKm(r.getLat(), r.getLon(),
                    shelter.getLat(), shelter.getLon());
            if (km > radiusKm)
                return false;
        }
        return true;
    }

    boolean filterFreshness(DonationBundle bundle, long arrivalMs) {
        for (FoodDonation d : bundle.getDonations()) {
            if (!GeoUtils.isSafeToDeliver(d, arrivalMs))
                return false;
        }
        return true;
    }

    MatchOption scoreAndSelect(List<MatchOption> options) {
        MatchOption best = null;
        for (MatchOption o : options) {
            if (best == null || isBetter(o, best)) {
                best = o;
            }
        }
        return best;
    }

    private boolean isBetter(MatchOption a, MatchOption b) {
        if (a.getTotalRouteKm() != b.getTotalRouteKm())
            return a.getTotalRouteKm() < b.getTotalRouteKm();
        if (a.getResidentsServed() != b.getResidentsServed())
            return a.getResidentsServed() > b.getResidentsServed();
        if (!a.getEarliestExpiry().equals(b.getEarliestExpiry()))
            return a.getEarliestExpiry().isBefore(b.getEarliestExpiry());
        return a.getPortionSurplus() < b.getPortionSurplus();
    }

    DeliveryOrder createDeliveryOrder(MatchOption winner) {
        long arrivalMs = GeoUtils.estimateArrivalMs(
                winner.getBundle().getRestaurantList(), winner.getShelter());
        int surplus = winner.getBundle().getTotalPortions()
                - winner.getShelter().getRemainingNeed();

        DeliveryOrder order = new DeliveryOrder(
                winner.getBundle(),
                winner.getShelter(),
                "KURIR-01",
                arrivalMs,
                Math.max(0, surplus));

        System.out.println("\n=== DELIVERY ORDER DIBUAT ===");
        System.out.println("  ID Order  : " + order.getOrderId());
        System.out.println("  Tujuan    : " + winner.getShelter().getName());
        System.out.printf("  Estimasi  : %.0f menit%n", arrivalMs / 60000.0);
        System.out.println("  Surplus   : " + Math.max(0, surplus) + " porsi");
        System.out.println("  Status    : WAITING_PICKUP");

        return order;
    }

    @Override
    public void sendAlert(String msg) {
        System.out.println("[ALERT] " + msg);
    }

    @Override
    public void onDonationExpired(FoodDonation d) {
        System.out.printf("[EXPIRED] Donasi %s (%s) telah kadaluarsa.%n",
                d.getDonationId(), d.getFoodName());
        auditLog.log("SYSTEM", ActionType.EXPIRE, d.getDonationId(), "Expired without delivery");
    }

    @Override
    public void onMatchFound(DeliveryOrder order) {
        System.out.println("[MATCH] Order " + order.getOrderId()
                + " dibuat untuk " + order.getShelter().getName());
        auditLog.log("SYSTEM", ActionType.MATCH, order.getOrderId(),
                "Delivery order created");
    }
}