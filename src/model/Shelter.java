package model;

import enums.ShelterType;

import java.util.LinkedList;
import java.util.List;

/**
 * Shelter — akun panti asuhan penerima donasi, extends User.
 * Fitur 3.2.2: registrasi, login/logout, riwayat penerimaan.
 * Fitur 3.2.5: resetDailyPortions saat startup.
 */
public class Shelter extends User {

    private String     name;
    private String     managerName;
    private double     lat;
    private double     lon;
    private int        residents;
    private ShelterType shelterType;
    private int        portionsToday;

    /** LinkedList — riwayat penerimaan, insert di head O(1) */
    private final LinkedList<DeliveryOrder> receiptHistory;

    public Shelter(String name, String managerName,
                   String username, String password,
                   String phone, String address,
                   double lat, double lon,
                   int residents, ShelterType shelterType) {
        super(username, password, phone, address);
        this.name          = name;
        this.managerName   = managerName;
        this.lat           = lat;
        this.lon           = lon;
        this.residents     = residents;
        this.shelterType   = shelterType;
        this.portionsToday = 0;
        this.receiptHistory = new LinkedList<>();
    }

    /**
     * Fitur 3.2.2 — Panti mengkonfirmasi penerimaan donasi, memberi rating dan catatan.
     * Output: status order DELIVERED, riwayat tersimpan di LinkedList.
     */
    public void confirmReceipt(DeliveryOrder order, int rating, String notes) {
        order.confirmDelivery(rating, notes);
        receiptHistory.addFirst(order);
    }

    /**
     * Fitur 3.2.2 — Riwayat penerimaan donasi beserta rating.
     * Output: tampilan riwayat penerimaan panti.
     */
    public List<DeliveryOrder> viewReceiptHistory() {
        return receiptHistory;
    }

    /** Fitur 3.2.2 — Perbarui jumlah penghuni aktif */
    public void updateResidents(int count) {
        this.residents = count;
        System.out.println("[✓] Jumlah penghuni diperbarui: " + count + " orang.");
    }

    /** Dipakai MatchingEngine — gate awal: apakah panti masih perlu porsi */
    public boolean canReceiveMore() {
        return portionsToday < residents;
    }

    /** Berapa porsi yang masih dibutuhkan panti malam ini */
    public int getRemainingNeed() {
        return Math.max(0, residents - portionsToday);
    }

    /**
     * Fitur 3.2.5 — Reset portionsToday = 0, dipanggil saat startup.
     * Memastikan sistem mulai dari data kebutuhan segar setiap sesi.
     */
    public void resetDailyPortions() {
        portionsToday = 0;
    }

    public void addPortionsToday(int p) { portionsToday += p; }

    public String      getName()        { return name; }
    public String      getManagerName() { return managerName; }
    public double      getLat()         { return lat; }
    public double      getLon()         { return lon; }
    public int         getResidents()   { return residents; }
    public ShelterType getShelterType() { return shelterType; }
    public int         getPortionsToday(){ return portionsToday; }

    @Override
    public String toString() {
        return String.format("Shelter{id='%s', name='%s', residents=%d, need=%d, status=%s}",
                userId, name, residents, getRemainingNeed(), accountStatus);
    }
}
