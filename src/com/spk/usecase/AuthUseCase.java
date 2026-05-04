package com.spk.usecase;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;

import org.mindrot.jbcrypt.BCrypt;

import com.spk.domain.User;
import com.spk.repository.UserRepository;

/**
 * Use case for authentication operations.
 */
public class AuthUseCase {
    private final UserRepository userRepository;
    private static User currentUser;

    public AuthUseCase() {
        this.userRepository = new UserRepository();
    }

    /**
     * Authenticate a user with username and password.
     * @return the authenticated User, or null if authentication fails.
     */
    public User login(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username tidak boleh kosong");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password tidak boleh kosong");
        }

        User user = userRepository.findByUsername(username.trim());
        if (user == null) {
            return null; // user not found
        }

        if (BCrypt.checkpw(password, user.getPassword())) {
            currentUser = user;
            String macAddress = getLocalMacAddress();
            String deviceType = resolveDeviceType();
            return user;
        }
        return null; // wrong password
    }

    private String getLocalMacAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface network : Collections.list(interfaces)) {
                if (network.isLoopback() || network.isVirtual() || !network.isUp()) {
                    continue;
                }
                byte[] address = network.getHardwareAddress();
                if (address == null || address.length == 0) {
                    continue;
                }
                StringBuilder builder = new StringBuilder();
                for (byte b : address) {
                    builder.append(String.format("%02X:", b));
                }
                builder.setLength(builder.length() - 1);
                return builder.toString();
            }
        } catch (SocketException ignored) {
        }
        return "UNKNOWN";
    }

    private String resolveDeviceType() {
        String osName = System.getProperty("os.name", "unknown").toLowerCase();
        if (osName.contains("android") || osName.contains("ios")) {
            return "Mobile";
        }
        return "Desktop";
    }

    /**
     * Log out the current user.
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Get the currently logged-in user.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if the current user is an admin.
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Change the current user's password.
     */
    public void changePassword(String oldPassword, String newPassword) throws SQLException {
        if (currentUser == null) {
            throw new IllegalStateException("Tidak ada user yang login");
        }
        if (!BCrypt.checkpw(oldPassword, currentUser.getPassword())) {
            throw new IllegalArgumentException("Password lama salah");
        }
        if (newPassword == null || newPassword.length() < 4) {
            throw new IllegalArgumentException("Password baru minimal 4 karakter");
        }

        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        userRepository.updatePassword(currentUser.getId(), hashed);
        currentUser.setPassword(hashed);
    }

    /**
     * Update current user's profile.
     */
    public void updateProfile(String fullName) throws SQLException {
        if (currentUser == null) {
            throw new IllegalStateException("Tidak ada user yang login");
        }
        currentUser.setFullName(fullName);
        userRepository.update(currentUser);
    }
}
