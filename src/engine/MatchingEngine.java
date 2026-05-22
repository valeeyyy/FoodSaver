package engine;

import datastructure.AuditLog;
import datastructure.DeliveryHistory;
import datastructure.DonationPool;
import datastructure.FoodExpiryTree;
import datastructure.ShelterRegistry;
import util.SystemConfig;

public class MatchingEngine {
    
    private DonationPool pool;
    private ShelterRegistry registry;
    private FoodExpiryTree expiryTree;
    private DeliveryHistory history;
    private AuditLog auditLog;

    // Constructor agar sesuai dengan pemanggilan di AppContext
    public MatchingEngine(DonationPool pool, ShelterRegistry registry, FoodExpiryTree expiryTree, DeliveryHistory history, AuditLog auditLog) {
        this.pool = pool;
        this.registry = registry;
        this.expiryTree = expiryTree;
        this.history = history;
        this.auditLog = auditLog;
    }

    public void run() {
        System.out.println("[MatchingEngine] Sedang memproses dan mencari panti asuhan yang cocok...");
    }

    public void runWithExpandedRadius() {
        System.out.println("[MatchingEngine] Menjalankan matching dengan radius diperluas: " + SystemConfig.EXPANDED_RADIUS_KM + " km");
    }
}