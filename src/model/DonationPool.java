package model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class DonationPool {

    private Queue<FoodDonation> queue;
    private int yellowAlertMinutes;
    private int redAlertMinutes;

    public DonationPool(Queue<FoodDonation> queue, int redAlertMinutes, int yellowAlertMinutes) {
        this.queue = (queue != null) ? queue : new LinkedList<>();
        this.redAlertMinutes = redAlertMinutes;
        this.yellowAlertMinutes = yellowAlertMinutes;
    }

    public void enqueue(FoodDonation donation) {
        queue.add(donation);
    }

    public FoodDonation dequeue() {
        return queue.poll();
    }

    public FoodDonation peek() {
        return queue.peek();
    }

    public void purgeExpired() {
        queue.removeIf(donation -> !donation.isStillFresh());
    }

    public void remove(FoodDonation donation) {
        queue.remove(donation);
    }

    public ArrayList<FoodDonation> getAll() {
        return new ArrayList<>(queue);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    public int getYellowAlertMinutes() {
        return yellowAlertMinutes;
    }

    public int getRedAlertMinutes() {
        return redAlertMinutes;
    }
}