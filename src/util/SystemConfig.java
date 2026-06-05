package util;

public class SystemConfig {
    public static final int MAX_FRESH_HOURS = 6;
    public static final double MAX_RADIUS_KM = 5.0;
    public static final double EXPANDED_RADIUS_KM = 8.0;
    public static final double COURIER_SPEED_KMH = 30.0;
    public static final int FRESHNESS_BUFFER_MIN = 30;
    public static final int LOADING_TIME_MINUTES = 5;
    public static final int YELLOW_ALERT_MINUTES = 120;
    public static final double KM_PER_DEGREE = 111.0;
    public static final String COURIER_ID = "KURIR-01";
    public static final int DEFAULT_RECEPTION_START_HOUR = 8;
    public static final int DEFAULT_RECEPTION_END_HOUR = 22;
    public static final double DOUBLE_EPSILON = 1e-9;
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";

    private SystemConfig() {
    }
}
