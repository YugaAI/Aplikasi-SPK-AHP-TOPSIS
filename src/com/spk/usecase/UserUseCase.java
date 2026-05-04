package com.spk.usecase;

import com.spk.domain.User;
import com.spk.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;

/**
 * Use case for user management (admin only).
 */
public class UserUseCase {
    private final UserRepository userRepository;

    public UserUseCase() {
        this.userRepository = new UserRepository();
    }

    public List<User> getAllUsers() throws SQLException {
        return userRepository.findAll();
    }

    public User getUserById(int id) throws SQLException {
        return userRepository.findById(id);
    }

    public void createUser(String username, String password, String fullName, String role) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username tidak boleh kosong");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Password minimal 4 karakter");
        }
        if (role == null || (!role.equals("admin") && !role.equals("user"))) {
            throw new IllegalArgumentException("Role harus 'admin' atau 'user'");
        }

        // Check duplicate username
        User existing = userRepository.findByUsername(username.trim());
        if (existing != null) {
            throw new IllegalArgumentException("Username '" + username + "' sudah digunakan");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setFullName(fullName != null ? fullName.trim() : "");
        user.setRole(role);
        userRepository.insert(user);
    }

    public void updateUser(int id, String username, String fullName, String role) throws SQLException {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User tidak ditemukan");
        }

        // Check duplicate username (exclude current user)
        User existing = userRepository.findByUsername(username.trim());
        if (existing != null && existing.getId() != id) {
            throw new IllegalArgumentException("Username '" + username + "' sudah digunakan");
        }

        user.setUsername(username.trim());
        user.setFullName(fullName != null ? fullName.trim() : "");
        user.setRole(role);
        userRepository.update(user);
    }

    public void resetPassword(int userId, String newPassword) throws SQLException {
        if (newPassword == null || newPassword.length() < 4) {
            throw new IllegalArgumentException("Password minimal 4 karakter");
        }
        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        userRepository.updatePassword(userId, hashed);
    }

    public void deleteUser(int id) throws SQLException {
        // Prevent deleting self
        User current = AuthUseCase.getCurrentUser();
        if (current != null && current.getId() == id) {
            throw new IllegalArgumentException("Tidak dapat menghapus akun sendiri");
        }
        userRepository.delete(id);
    }
}
