package com.greenlink.greenlink.service;

import com.greenlink.greenlink.config.TestConfig;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
public class UserServiceTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FileStorageService fileStorageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
    }

    @Test
    void testRegisterUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        User savedUser = userService.registerUser(user);

        assertNotNull(savedUser.getId());
        assertEquals("test@example.com", savedUser.getEmail());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }

    @Test
    void testRegisterDuplicateEmail() {
        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        User user = new User();
        user.setEmail("duplicate@example.com");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");

        assertThrows(RuntimeException.class, () -> userService.registerUser(user));
    }

    @Test
    @WithMockUser(username = "update@example.com")
    void testUpdateProfile() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("update@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setFirstName("Original");
        existingUser.setLastName("Name");

        when(userRepository.findByEmail("update@example.com")).thenReturn(Optional.of(existingUser));

        User updateData = new User();
        updateData.setFirstName("Updated");
        updateData.setLastName("Profile");
        updateData.setBio("New bio");

        User updatedUser = userService.updateProfile("update@example.com", updateData, null);

        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Profile", updatedUser.getLastName());
        assertEquals("New bio", updatedUser.getBio());
    }

    @Test
    void testGetUserByEmail() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("find@example.com");
        mockUser.setPassword(passwordEncoder.encode("password123"));
        mockUser.setFirstName("Find");
        mockUser.setLastName("Me");

        when(userRepository.findByEmail("find@example.com")).thenReturn(Optional.of(mockUser));

        User foundUser = userService.getUserByEmail("find@example.com");

        assertNotNull(foundUser);
        assertEquals("find@example.com", foundUser.getEmail());
        assertEquals("Find", foundUser.getFirstName());
        assertEquals("Me", foundUser.getLastName());
    }

    @Test
    @WithMockUser(username = "password@example.com")
    void testChangePassword() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("password@example.com");
        mockUser.setPassword(passwordEncoder.encode("oldPassword"));

        when(userRepository.findByEmail("password@example.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        boolean changed = userService.changePassword("oldPassword", "newPassword");

        assertTrue(changed);
        assertTrue(passwordEncoder.matches("newPassword", mockUser.getPassword()));
    }
} 