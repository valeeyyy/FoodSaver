package model;

import enums.AccountStatus;
import util.IdGenerator;

import java.time.LocalDateTime;
import model.EditRequest;

public abstract class User {

    protected String userId;
    protected String username;
    protected String password;
    protected String phone;
    protected String address;
    protected AccountStatus accountStatus;
    protected LocalDateTime registeredAt;

    public User(String username, String password, String phone, String address) {
        this.userId = IdGenerator.nextUserId(getClass().getSimpleName().toUpperCase().substring(0, 3));
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.accountStatus = AccountStatus.PENDING;
        this.registeredAt = LocalDateTime.now();
    }

    // Proposal §1 (User): memvalidasi kombinasi username + password dan
    // memastikan status akun bukan REJECTED. Akun REJECTED langsung ditolak.
    public boolean login(String inputUsername, String inputPassword) {
        if (accountStatus == AccountStatus.REJECTED) {
            return false;
        }
        return username.equals(inputUsername) && password.equals(inputPassword);
    }

    public void logout() {
        System.out.println("[✓] Logout berhasil. Sampai jumpa, " + username + "!");
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setAccountStatus(AccountStatus status) {
        this.accountStatus = status;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private EditRequest pendingEditRequest = null;

    public EditRequest getPendingEditRequest() {
        return pendingEditRequest;
    }

    public void setPendingEditRequest(EditRequest req) {
        this.pendingEditRequest = req;
    }

    public boolean hasPendingEdit() {
        return pendingEditRequest != null
                && pendingEditRequest.getStatus() == EditRequest.EditStatus.PENDING;
    }
}