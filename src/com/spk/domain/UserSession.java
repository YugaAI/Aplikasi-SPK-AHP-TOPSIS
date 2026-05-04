package com.spk.domain;

/**
 * Entity representing a user session or device login.
 */
public class UserSession {
    private int id;
    private int userId;
    private String macAddress;
    private String deviceType;
    private String lastLogin;

    public UserSession() {
    }

    public UserSession(int userId, String macAddress, String deviceType) {
        this.userId = userId;
        this.macAddress = macAddress;
        this.deviceType = deviceType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }
}
