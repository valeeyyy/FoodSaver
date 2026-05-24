package model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class EditRequest {

    public enum EditStatus { PENDING, APPROVED, REJECTED }

    private final String requestId;
    private final User requester;
    private final Map<String, String> newData;
    private EditStatus status;
    private final LocalDateTime requestedAt;
    private String adminNotes;

    public EditRequest(User requester, Map<String, String> newData) {
        this.requestId = "EDIT-" + requester.getUsername().toUpperCase()
                + "-" + System.currentTimeMillis();
        this.requester = requester;
        this.newData = new HashMap<>(newData);
        this.status = EditStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    public String getRequestId()       { return requestId; }
    public User getRequester()         { return requester; }
    public Map<String, String> getNewData() { return newData; }
    public EditStatus getStatus()      { return status; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public String getAdminNotes()      { return adminNotes; }

    public void approve(String notes) {
        this.status = EditStatus.APPROVED;
        this.adminNotes = notes;
    }

    public void reject(String notes) {
        this.status = EditStatus.REJECTED;
        this.adminNotes = notes;
    }

    @Override
    public String toString() {
        return String.format("EditRequest{id='%s', user='%s', status=%s, data=%s}",
                requestId, requester.getUsername(), status, newData);
    }
}
