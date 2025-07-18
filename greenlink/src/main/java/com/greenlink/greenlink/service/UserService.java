package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface UserService extends UserDetailsService {
    User registerUser(User user);
    User getCurrentUser();
    User getCurrentUser(String email);
    User updateProfile(User user);
    User updateProfile(String email, User user, MultipartFile profilePicture);
    boolean changePassword(String oldPassword, String newPassword);
    User createUser(User user);
    User getUserById(Long id);
    User getUserByEmail(String email);
    void addPoints(Long userId, int points);
    void deductPoints(Long userId, int points);
    List<User> getTopUsers(int limit);
    List<User> getTopUsersByLevel(int limit);
    List<User> getUsersByMinLevel(int minLevel);
    boolean emailExists(String email);
    void deactivateAccount(Long userId);
    void reactivateAccount(Long userId);
}