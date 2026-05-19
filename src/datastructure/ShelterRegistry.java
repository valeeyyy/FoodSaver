package datastructure;

import model.Shelter;

import java.util.*;

public class ShelterRegistry {

    private final Map<String, Shelter> map;

    public ShelterRegistry() {
        this.map = new HashMap<>();
    }

    public void register(Shelter s) {
        map.put(s.getUserId(), s);
    }

    public void remove(Shelter s) { 
        map.remove(s.getUserId()); 
    }

    public void remove(String id) { 
        map.remove(id); 
    }

    public Collection<Shelter> getAll() {
        return map.values();
    }
}