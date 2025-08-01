package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.model.Role;
import com.greenlink.greenlink.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final PointsService pointsService;
    private final FriendService friendService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, FileStorageService fileStorageService, PointsService pointsService, FriendService friendService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
        this.pointsService = pointsService;
        this.friendService = friendService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Attempting to load user by email: {}", email);
        try {
            logger.debug("Searching for user in database with email: {}", email);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("Login failed - User not found with email: {}", email);
                        return new UsernameNotFoundException("User not found with email: " + email);
                    });
            
            logger.info("User found with details: Email={}, Enabled={}, Active={}, Role={}, PasswordHash={}",
                user.getEmail(),
                user.isEnabled(), 
                user.isActive(),
                user.getRole(),
                user.getPassword());

            if (!user.isEnabled()) {
                logger.error("Login failed - User account is disabled: {}", email);
                throw new UsernameNotFoundException("User account is disabled");
            }
            
            if (!user.isActive()) {
                logger.error("Login failed - User account is not active: {}", email);
                throw new UsernameNotFoundException("User account is not active");
            }

            
            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
            logger.info("User authorities: {}", authorities);
            
            logger.info("Successfully loaded user: {}", email);
            return user;
        } catch (Exception e) {
            logger.error("Error during user authentication for email {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public User registerUser(User user) {
        logger.info("Registering new user with email: {}", user.getEmail());
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("Registration failed - email already exists: {}", user.getEmail());
            throw new RuntimeException("Email already registered");
        }

        
        String rawPassword = user.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        logger.info("Password encoding - Raw Length: {}, Encoded Length: {}", 
            rawPassword.length(), encodedPassword.length());
        
        user.setPassword(encodedPassword);
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setActive(true);
        user.setPoints(0);
        user.setProfilePicture("/images/default-avatar.svg");
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {}", user.getEmail());
        return savedUser;
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("No authenticated user found");
        }
        return userRepository.findByEmailWithFavorites(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    @Override
    public User getCurrentUser(String email) {
        return userRepository.findByEmailWithFavorites(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public User updateProfile(User updatedUser) {
        User currentUser = getCurrentUser();
        currentUser.setFirstName(updatedUser.getFirstName());
        currentUser.setLastName(updatedUser.getLastName());
        currentUser.setBio(updatedUser.getBio());
        if (updatedUser.getProfilePicture() != null) {
            currentUser.setProfilePicture(updatedUser.getProfilePicture());
        }
        return userRepository.save(currentUser);
    }

    @Override
    @Transactional
    public User updateProfile(String email, User userUpdate, MultipartFile profilePicture) {
        User existingUser = getCurrentUser(email);
        existingUser.setFirstName(userUpdate.getFirstName());
        existingUser.setLastName(userUpdate.getLastName());
        existingUser.setBio(userUpdate.getBio());

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String fileName = fileStorageService.storeFile(profilePicture);
            // Use the /files/profiles/ endpoint for serving profile pictures
            existingUser.setProfilePicture("/files/profiles/" + fileName);
        }

        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public boolean changePassword(String oldPassword, String newPassword) {
        User currentUser = getCurrentUser();
        if (passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            currentUser.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(currentUser);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public User createUser(User user) {
        logger.debug("Creating new user with email: {}", user.getEmail());
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("User creation failed - email already exists: {}", user.getEmail());
            throw new RuntimeException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setActive(true);
        user.setPoints(0);
        user.setProfilePicture("/images/default-avatar.svg");
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        logger.info("User created successfully: {}", user.getEmail());
        return savedUser;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public void addPoints(Long userId, int points) {
        
        pointsService.addPoints(userId, points, "MANUAL", "Points added manually");
    }

    @Override
    @Transactional
    public void deductPoints(Long userId, int points) {
        User user = getUserById(userId);
        if (user.getPoints() < points) {
            throw new RuntimeException("Insufficient points");
        }
        
        pointsService.addPoints(userId, -points, "MANUAL_DEDUCTION", "Points deducted manually");
    }

    @Override
    public List<User> getTopUsers(int limit) {
        return userRepository.findTopByOrderByPointsDesc(limit);
    }

    @Override
    public List<User> getTopUsersByLevel(int limit) {
        return userRepository.findTopByOrderByLevelDesc(limit);
    }

    @Override
    public List<User> getUsersByMinLevel(int minLevel) {
        return userRepository.findByLevelGreaterThanEqualOrderByLevelDescPointsDesc(minLevel);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId) {
        User user = getUserById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void reactivateAccount(Long userId) {
        User user = getUserById(userId);
        user.setActive(true);
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void recalculateUserLevel(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        int newLevel = user.calculateLevel(user.getPoints());
        user.setLevel(newLevel);
        userRepository.save(user);
        
        logger.info("Recalculated level for user {}: {} points -> level {}", 
                   userId, user.getPoints(), newLevel);
    }

    @Override
    @Transactional
    public void recalculateAllUserLevels() {
        List<User> allUsers = userRepository.findAll();
        int updatedCount = 0;
        
        for (User user : allUsers) {
            int oldLevel = user.getLevel();
            int newLevel = user.calculateLevel(user.getPoints());
            
            if (oldLevel != newLevel) {
                user.setLevel(newLevel);
                userRepository.save(user);
                updatedCount++;
                
                logger.info("Updated user {} level: {} -> {} ({} points)", 
                           user.getId(), oldLevel, newLevel, user.getPoints());
            }
        }
        
        logger.info("Recalculated levels for {} users", updatedCount);
    }

    @Override
    public int getUserRank(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            return -1; 
        }
        
        
        long usersWithMorePoints = userRepository.countByPointsGreaterThan(user.getPoints());
        
                
        return (int) (usersWithMorePoints + 1);
    }

    @Override
    public long getTotalUserCount() {
        return userRepository.count();
    }

    @Override
    public long getTotalActiveUserCount() {
        return userRepository.countByActiveTrue();
    }

    @Override
    public long getTotalPoints() {
        return userRepository.findAll().stream()
                .mapToLong(User::getPoints)
                .sum();
    }

    @Override
    public long getTotalLevels() {
        return userRepository.findAll().stream()
                .mapToLong(User::getLevel)
                .sum();
    }

    @Override
    public List<User> getFriendsLeaderboard(Long userId, int limit) {
        User user = getUserById(userId);
        if (user == null) {
            return List.of();
        }
        
        List<User> friends = friendService.getFriendsList(user);
        
        // Sort friends by points in descending order and limit the results
        return friends.stream()
                .sorted((u1, u2) -> Integer.compare(u2.getPoints(), u1.getPoints()))
                .limit(limit)
                .toList();
    }
} 