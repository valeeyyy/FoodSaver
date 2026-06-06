package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IdGenerator {

    private static int donationCounter = 0;
    private static int orderCounter = 0;
    private static int bundleCounter = 0;
    private static int userCounter = 0;
    private static int entryCounter = 0;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private IdGenerator() {
    }

    public static String nextDonationId() {
        return String.format("DON-%s-%03d",
                LocalDateTime.now().format(FMT), ++donationCounter);
    }

    public static String nextOrderId() {
        return String.format("ORD-%s-%03d",
                LocalDateTime.now().format(FMT), ++orderCounter);
    }

    public static String nextBundleId() {
        return String.format("BND-%s-%03d",
                LocalDateTime.now().format(FMT), ++bundleCounter);
    }

    public static String nextUserId(String prefix) {
        return String.format("%s-%04d", prefix, ++userCounter);
    }

    public static String nextEntryId() {
        return String.format("AUD-%05d", ++entryCounter);
    }
}
