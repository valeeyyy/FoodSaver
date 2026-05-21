package datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Shelter;

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

    public List<Shelter> getActiveShelters(){
        return new ArrayList<>(map.values());
    }
}