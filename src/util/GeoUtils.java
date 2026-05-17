package util;

import model.FoodDonation;
import model.Restaurant;
import model.Shelter;

import java.util.List;

public class GeoUtils {

    private GeoUtils() {
    }

    public static double euclideanKm(double lat1, double lon1,
            double lat2, double lon2) {
        double dLat = (lat2 - lat1) * 111.0;
        double dLon = (lon2 - lon1) * 111.0 * Math.cos(Math.toRadians(lat1));
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }

    public static long estimateArrivalMs(List<Restaurant> pickups, Shelter shelter) {
        if (pickups == null || pickups.isEmpty())
            return 0;

        double totalKm = 0.0;
        for (int i = 0; i < pickups.size() - 1; i++) {
            Restaurant a = pickups.get(i);
            Restaurant b = pickups.get(i + 1);
            totalKm += euclideanKm(a.getLat(), a.getLon(), b.getLat(), b.getLon());
        }
        Restaurant last = pickups.get(pickups.size() - 1);
        totalKm += euclideanKm(last.getLat(), last.getLon(), shelter.getLat(), shelter.getLon());

        double travelMinutes = (totalKm / SystemConfig.COURIER_SPEED_KMH) * 60.0;
        double loadingMinutes = (double) pickups.size() * SystemConfig.LOADING_TIME_MINUTES;

        double totalMinutes = travelMinutes + loadingMinutes;
        return (long) (totalMinutes * 60 * 1000); // convert to ms
    }

    public static boolean isSafeToDeliver(FoodDonation d, long arrivalMs) {
        long expiredEpoch = d.getExpiredAt()
                .minusMinutes(SystemConfig.FRESHNESS_BUFFER_MIN)
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        long arrivalEpoch = System.currentTimeMillis() + arrivalMs;
        return arrivalEpoch <= expiredEpoch;
    }

    public static double getTotalRouteKm(List<Restaurant> pickups, Shelter shelter) {
        if (pickups == null || pickups.isEmpty())
            return 0.0;
        double totalKm = 0.0;
        for (int i = 0; i < pickups.size() - 1; i++) {
            Restaurant a = pickups.get(i);
            Restaurant b = pickups.get(i + 1);
            totalKm += euclideanKm(a.getLat(), a.getLon(), b.getLat(), b.getLon());
        }
        Restaurant last = pickups.get(pickups.size() - 1);
        totalKm += euclideanKm(last.getLat(), last.getLon(), shelter.getLat(), shelter.getLon());
        return totalKm;
    }
}
