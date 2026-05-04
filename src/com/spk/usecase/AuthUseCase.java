package com.spk.usecase;

import java.sql.SQLException;

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
            return user;
        }
        return null; // wrong password
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
