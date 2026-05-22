package datastructure;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Shelter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ShelterRegistry — wrapper HashMap yang menyimpan semua panti terdaftar.
 * Key = shelterId → akses O(1).
 *
 * Fitur 3.2.2: register() saat panti mendaftar, findById() saat admin mencari panti.
 */
public class ShelterRegistry {

    /** HashMap — key=shelterId, akses O(1) */
    private final Map<String, Shelter> map;

    public ShelterRegistry() {
        this.map = new HashMap<>();
    }

    /** Fitur 3.2.2 — Daftarkan panti baru ke HashMap */
    public void register(Shelter s) {
        map.put(s.getUserId(), s);
    }

    public void remove(String id) {
        map.remove(id);
    }

    /**
     * Fitur 3.2.2 — Pencarian panti O(1) via HashMap.
     * Dipakai Admin.searchShelterById().
     */
    public Shelter findById(String id) {
        return map.get(id);
    }

    /** Semua panti yang canReceiveMore() == true — dipakai MatchingEngine */
    public List<Shelter> findAllEligible() {
        List<Shelter> result = new ArrayList<>();
        for (Shelter s : map.values()) {
            if (s.canReceiveMore()) result.add(s);
        }
        return result;
    }

    public Collection<Shelter> getAll() {
        return map.values();
    }

    public boolean contains(String id) {
        return map.containsKey(id);
    }
}
