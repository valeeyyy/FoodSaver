package datastructure;

import enums.AccountStatus;
import model.Shelter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShelterRegistry {

    private final Map<String, Shelter> map;

    public ShelterRegistry() {
        this.map = new HashMap<>();
    }

    public void register(Shelter s) {
        map.put(s.getUserId(), s);
    }

    public void remove(String id) {
        map.remove(id);
    }

    public Shelter findById(String id) {
        return map.get(id);
    }

    public List<Shelter> findAllEligible() {
        List<Shelter> result = new ArrayList<>();
        for (Shelter s : map.values()) {
            if (s.getAccountStatus() == AccountStatus.APPROVED && s.canReceiveMore())
                result.add(s);
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
