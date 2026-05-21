package engine;

import datastructure.AuditLog;
import datastructure.DeliveryHistory;
import datastructure.DonationPool;
import datastructure.FoodExpiryTree;
import datastructure.ShelterRegistry;
import java.util.ArrayList;
import java.util.List;
import model.DeliveryOrder;
import model.DonationBundle;
import model.Shelter;
import util.SystemConfig;

public class MatchingEngine {

    private DonationPool pool;
    private ShelterRegistry registry;
    private FoodExpiryTree expiryTree;
    private DeliveryHistory history;
    private AuditLog auditLog;
    private SystemConfig config;

    public MatchingEngine(DonationPool pool, ShelterRegistry registry, FoodExpiryTree expiryTree,
            DeliveryHistory history, AuditLog auditLog, SystemConfig config) {
        this.pool = pool;
        this.registry = registry;
        this.expiryTree = expiryTree;
        this.history = history;
        this.auditLog = auditLog;
        this.config = config;
    }

    public void run() {
        boolean matchFound = true;
        while (matchFound) {
            matchFound = processMatchingLoop(5.0);
        }
    }

    public void runWithRadius(double radiusKm) {
        boolean matchFound = true;
        while (matchFound) {
            matchFound = processMatchingLoop(radiusKm);
        }
    }

    public void runWithExpandedRadius() {
        System.out.println("[MatchingEngine] Menjalankan matching dengan radius diperluas: "
                + SystemConfig.EXPANDED_RADIUS_KM + " km");
        runWithRadius(SystemConfig.EXPANDED_RADIUS_KM);
    }

    private boolean processMatchingLoop(double radiusKm) {
        List<DonationBundle> combinations = pool.generateAllPossibleBundles();
        List<Shelter> shelters = registry.getActiveShelters();

        List<MatchOption> validOptions = new ArrayList<>();

        for (DonationBundle bundle : combinations) {
            for (Shelter shelter : shelters) {

                boolean isPorsiCukup = filterPortions(bundle, shelter);
                boolean isJarakAman = filterDistance(bundle, shelter, radiusKm);

                if (isPorsiCukup && isJarakAman) {
                    double routeKm = 2.5; 
                    long arrivalMs = 10000; 

                    MatchOption option = new MatchOption(bundle, shelter, routeKm, arrivalMs);
                    validOptions.add(option);
                }
            }
        }

        if (validOptions.isEmpty()) {
            return false;
        }

        MatchOption winner = scoreAndSelect(validOptions);

        DeliveryOrder order = new DeliveryOrder(winner);
        order.assignCourier("Kurir Otomatis");
        history.addOrder(order);

        return true;
    }

    public boolean filterPortions(DonationBundle bundle, Shelter shelter) {
        if (bundle.getTotalPortions() >= shelter.getRemainingNeed()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean filterDistance(DonationBundle bundle, Shelter shelter, double radiusKm) {
        return true;
    }

    public MatchOption scoreAndSelect(List<MatchOption> options) {
        MatchOption bestOption = options.get(0);

        for (int i = 1; i < options.size(); i++) {
            MatchOption currentOption = options.get(i);

            if (currentOption.getTotalRouteKm() < bestOption.getTotalRouteKm()) {
                bestOption = currentOption;
            }
            else if (currentOption.getTotalRouteKm() == bestOption.getTotalRouteKm()) {
                if (currentOption.getResidentsServed() > bestOption.getResidentsServed()) {
                    bestOption = currentOption;
                }
            }
        }

        return bestOption;
    }
}