package com.greenlink.greenlink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;

    @NotBlank
    @Size(max = 50)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    private String lastName;

    private String bio;

    private String profilePicture;

    @Column(nullable = false)
    private int points = 0;

    @Column(nullable = false)
    private int level = 1;

    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;

    private boolean enabled = true;

    private boolean active = true;

    @OneToMany(mappedBy = "user")
    private List<QuizResult> quizResults;

    @Transient
    private Map<String, Object> additionalProperties = new HashMap<>();

    @OneToMany(mappedBy = "user")
    private List<Challenge> challenges;
    
    @OneToMany(mappedBy = "seller")
    private List<Product> products = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "favorite_products",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> favoriteProducts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friend> friends = new ArrayList<>();

    @OneToMany(mappedBy = "friendUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friend> friendOf = new ArrayList<>();

    // Stripe Connect fields
    @Column(name = "stripe_account_id")
    private String stripeAccountId;
    
    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    public User() {
    }

    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // UserDetails interface implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());
        return List.of(authority);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
        this.level = calculateLevel(points);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Calculate user level based on total points
     * Level thresholds: 1-50=1, 51-150=2, 151-300=3, 301-500=4, 501-750=5, 751-1050=6, etc.
     */
    public int calculateLevel(int totalPoints) {
        if (totalPoints <= 0) return 1;
        
        // Progressive level system: each level requires more points
        // Level 1: 0-50 points
        // Level 2: 51-150 points (100 more)
        // Level 3: 151-300 points (150 more)
        // Level 4: 301-500 points (200 more)
        // Level 5: 501-750 points (250 more)
        // Level 6: 751-1050 points (300 more)
        // etc.
        
        int level = 1;
        int pointsNeeded = 50;
        int totalPointsNeeded = 0;
        
        while (totalPoints >= totalPointsNeeded + pointsNeeded) {
            totalPointsNeeded += pointsNeeded;
            level++;
            pointsNeeded += 50 * level;
        }
        
        return level;
    }

    /**
     * Get points needed for next level
     */
    public int getPointsForNextLevel() {
        int currentLevel = this.level;
        int pointsNeeded = 50;
        int totalPointsNeeded = 0;
        
        for (int i = 1; i <= currentLevel; i++) {
            totalPointsNeeded += pointsNeeded;
            pointsNeeded += 50 * i;
        }
        
        return totalPointsNeeded;
    }

    /**
     * Get progress percentage to next level
     */
    public int getProgressToNextLevel() {
        if (this.points <= 0) return 0;
        
        int pointsForCurrentLevel = getPointsForCurrentLevel();
        int pointsForNextLevel = getPointsForNextLevel();
        int pointsInCurrentLevel = this.points - pointsForCurrentLevel;
        int pointsNeededForNextLevel = pointsForNextLevel - pointsForCurrentLevel;
        
        if (pointsNeededForNextLevel <= 0) return 100;
        
        int progress = (pointsInCurrentLevel * 100) / pointsNeededForNextLevel;
        return Math.min(100, Math.max(0, progress));
    }

    /**
     * Get points needed for current level
     */
    private int getPointsForCurrentLevel() {
        int currentLevel = this.level;
        int pointsNeeded = 50;
        int totalPointsNeeded = 0;
        
        for (int i = 1; i < currentLevel; i++) {
            totalPointsNeeded += pointsNeeded;
            pointsNeeded += 50 * i;
        }
        
        return totalPointsNeeded;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperty(String key, Object value) {
        this.additionalProperties.put(key, value);
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public List<QuizResult> getQuizResults() {
        return quizResults;
    }

    public void setQuizResults(List<QuizResult> quizResults) {
        this.quizResults = quizResults;
    }

    public List<Challenge> getChallenges() {
        return challenges;
    }

    public void setChallenges(List<Challenge> challenges) {
        this.challenges = challenges;
    }
    
    public List<Product> getProducts() {
        return products;
    }
    
    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Product> getFavoriteProducts() {
        return favoriteProducts;
    }

    public void setFavoriteProducts(List<Product> favoriteProducts) {
        this.favoriteProducts = favoriteProducts;
    }

    public String getStripeAccountId() {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }
}