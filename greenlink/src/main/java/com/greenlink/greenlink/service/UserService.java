package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // Metodă pentru crearea unui nou utilizator
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Există deja un cont cu acest email");
        }

        // Setează valorile inițiale
        user.setPoints(0);
        user.setRegisteredAt(LocalDateTime.now());
        user.setProfilePicture("/images/default-avatar.png"); // Imagine default

        return userRepository.save(user);
    }

    // Găsește utilizator după ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit"));
    }

    // Găsește utilizator după email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit"));
    }

    // Obține utilizatorul curent
    public User getCurrentUser(String email) {
        return getUserByEmail(email);
    }

    // Actualizează profilul utilizatorului
    @Transactional
    public User updateProfile(String email, User userUpdate, MultipartFile profilePicture) {
        User existingUser = getUserByEmail(email);

        // Actualizează informațiile de bază
        existingUser.setFirstName(userUpdate.getFirstName());
        existingUser.setLastName(userUpdate.getLastName());
        existingUser.setBio(userUpdate.getBio());

        // Procesează imaginea de profil dacă există
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String fileName = fileStorageService.storeFile(profilePicture);
            existingUser.setProfilePicture("/uploads/profiles/" + fileName);
        }

        return userRepository.save(existingUser);
    }

    // Adaugă puncte utilizatorului
    @Transactional
    public void addPoints(Long userId, int points) {
        User user = getUserById(userId);
        user.setPoints(user.getPoints() + points);
        userRepository.save(user);
    }

    // Scade puncte utilizatorului
    @Transactional
    public void deductPoints(Long userId, int points) {
        User user = getUserById(userId);
        if (user.getPoints() < points) {
            throw new RuntimeException("Nu ai suficiente puncte");
        }
        user.setPoints(user.getPoints() - points);
        userRepository.save(user);
    }

    // Obține top utilizatori după puncte
    public List<User> getTopUsers(int limit) {
        return userRepository.findTopByOrderByPointsDesc(limit);
    }

    // Verifică dacă emailul există
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // Dezactivează cont utilizator
    @Transactional
    public void deactivateAccount(Long userId) {
        User user = getUserById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    // Reactivează cont utilizator
    @Transactional
    public void reactivateAccount(Long userId) {
        User user = getUserById(userId);
        user.setActive(true);
        userRepository.save(user);
    }
}