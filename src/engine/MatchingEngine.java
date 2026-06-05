package engine;

import datastructure.AuditLog;
import datastructure.DeliveryHistory;
import datastructure.DonationPool;
import datastructure.FoodExpiryTree;
import datastructure.ShelterRegistry;
import enums.ActionType;
import java.util.ArrayList;
import java.util.List;
import model.*;
import util.GeoUtils;
import util.SystemConfig;

public class MatchingEngine implements Notifiable {

    private static final String BOX_TOP = "┌─── DELIVERY ORDER DIBUAT " + "─".repeat(36) + "┐";
    private static final String BOX_BOT = "└" + "─".repeat(62) + "┘";
    private static final String ROW_FMT = "│  %-10s: %-48s│";

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
        System.out.println("[Engine] Radius diperluas ke " + SystemConfig.EXPANDED_RADIUS_KM + " km");
        runWithRadius(SystemConfig.EXPANDED_RADIUS_KM);
    }

    private void runWithRadius(double radiusKm) {
        System.out.println("\n=== MATCHING ENGINE (radius=" + radiusKm + " km) ===");

        while (true) {
            List<Shelter> eligible = registry.findAllEligible();
            if (eligible.isEmpty()) {
                System.out.println("[!] Tidak ada panti yang membutuhkan donasi.");
                break;
            }

            List<FoodDonation> active = expiryTree.getByPriority();
            if (active.isEmpty()) {
                System.out.println("[!] Tidak ada donasi aktif.");
                break;
            }

            List<MatchOption> options = new ArrayList<>();
            for (DonationBundle bundle : generateCombinations(active)) {
                for (Shelter shelter : eligible) {
                    if (applyFilters(bundle, shelter, radiusKm)) {
                        double km = GeoUtils.getTotalRouteKm(bundle.getRestaurantList(), shelter);
                        long ms = GeoUtils.estimateArrivalMs(bundle.getRestaurantList(), shelter);
                        options.add(new MatchOption(bundle, shelter, km, ms));
                    }
                }
            }

            if (options.isEmpty()) {
                System.out.println("[✗] Tidak ada pasangan donasi-panti yang valid.");
                break;
            }

            MatchOption winner = scoreAndSelect(options);
            System.out.println("[✓] Match ditemukan: " + winner);
            DeliveryOrder order = createDeliveryOrder(winner);
            history.addFirst(order);

            for (FoodDonation d : winner.getBundle().getDonations()) {
                d.markAsMatched();
                pool.remove(d);
                expiryTree.remove(d);
            }

            int portionsDelivered = winner.getBundle().getTotalPortions()
                    - Math.max(0, winner.getPortionSurplus());
            winner.getShelter().addPortionsToday(portionsDelivered);
            onMatchFound(order);

            if (pool.isEmpty()) break;
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
        long arrivalMs = GeoUtils.estimateArrivalMs(bundle.getRestaurantList(), shelter);
        return filterPortions(bundle, shelter)
                && filterDistance(bundle, shelter, radiusKm)
                && filterFreshness(bundle, arrivalMs, shelter);
    }

    boolean filterPortions(DonationBundle bundle, Shelter shelter) {
        return bundle.getTotalPortions() > 0;
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

    boolean filterFreshness(DonationBundle bundle, long arrivalMs, Shelter shelter) {
        for (FoodDonation d : bundle.getDonations()) {
            if (!GeoUtils.isSafeToDeliver(d, arrivalMs, shelter))
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
        // P1: rute terpendek
        if (Math.abs(a.getTotalRouteKm() - b.getTotalRouteKm()) > SystemConfig.DOUBLE_EPSILON)
            return a.getTotalRouteKm() < b.getTotalRouteKm();
        // P2: penghuni terbanyak
        if (a.getResidentsServed() != b.getResidentsServed())
            return a.getResidentsServed() > b.getResidentsServed();
        // P3: paling cepat expired
        if (!a.getEarliestExpiry().equals(b.getEarliestExpiry()))
            return a.getEarliestExpiry().isBefore(b.getEarliestExpiry());
        // P4: surplus terkecil; pengiriman penuh diutamakan atas parsial
        int sa = a.getPortionSurplus(), sb = b.getPortionSurplus();
        if (sa >= 0 && sb < 0) return true;
        if (sa < 0 && sb >= 0) return false;
        return Math.abs(sa) < Math.abs(sb);
    }

    DeliveryOrder createDeliveryOrder(MatchOption winner) {
        long arrivalMs = GeoUtils.estimateArrivalMs(
                winner.getBundle().getRestaurantList(), winner.getShelter());
        int surplus = winner.getBundle().getTotalPortions()
                - winner.getShelter().getRemainingNeed();

        DeliveryOrder order = new DeliveryOrder(
                winner.getBundle(),
                winner.getShelter(),
                SystemConfig.COURIER_ID,
                arrivalMs,
                Math.max(0, surplus));

        System.out.println();
        System.out.println(BOX_TOP);
        System.out.printf(ROW_FMT + "%n", "ID Order", order.getOrderId());
        System.out.printf(ROW_FMT + "%n", "Tujuan", winner.getShelter().getName());
        System.out.printf(ROW_FMT + "%n", "Jarak", String.format("%.2f km", winner.getTotalRouteKm()));
        System.out.printf(ROW_FMT + "%n", "Estimasi", String.format("%.0f menit", arrivalMs / 60000.0));
        System.out.printf(ROW_FMT + "%n", "Surplus", Math.max(0, surplus) + " porsi");
        System.out.printf(ROW_FMT + "%n", "Status", "WAITING_PICKUP");
        System.out.println(BOX_BOT);

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
        auditLog.log(new AuditEntry("SYSTEM", ActionType.EXPIRE,
                d.getDonationId(), "Expired without delivery",
                enums.DonationStatus.EXPIRED_UNDELIVERED));
    }

    @Override
    public void onMatchFound(DeliveryOrder order) {
        System.out.println("[MATCH] Order " + order.getOrderId()
                + " dibuat untuk " + order.getShelter().getName());
        auditLog.log("SYSTEM", ActionType.MATCH, order.getOrderId(),
                "Delivery order created");
    }
}