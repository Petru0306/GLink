package com.greenlink.greenlink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
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
    // Validation constraints only apply for form submissions, not for OAuth2 users
    // These are skipped when entities are directly saved in repositories
    @Size(min = 8, max = 120, message = "Password must be at least 8 characters long")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%&+=!.,])(?=\\S+$).{8,}$",
        message = "Password must contain at least one number, one lowercase letter, one uppercase letter, and one special character (@#$%&+=!.,)"
    )
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

    // OAuth2 fields
    @Column(name = "oauth2_provider")
    private String oauth2Provider;
    
    @Column(name = "oauth2_provider_id")
    private String oauth2ProviderId;
    
    @Column(name = "oauth2_name")
    private String oauth2Name;
    
    @Column(name = "oauth2_picture")
    private String oauth2Picture;

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
     * New 15-level system with specific point thresholds
     */
    public int calculateLevel(int totalPoints) {
        if (totalPoints <= 0) return 1;
        
        // New level thresholds: 50, 150, 300, 500, 750, 1050, 1400, 1800, 2250, 2750, 3300, 3900, 4550, 5250, 6000
        int[] levelThresholds = {50, 150, 300, 500, 750, 1050, 1400, 1800, 2250, 2750, 3300, 3900, 4550, 5250, 6000};
        
        for (int i = 0; i < levelThresholds.length; i++) {
            if (totalPoints < levelThresholds[i]) {
                return i + 1;
            }
        }
        
        return 15; // Max level
    }

    /**
     * Get level name based on level number
     */
    public String getLevelName() {
        return getLevelName(this.level);
    }

    /**
     * Get level name for a specific level number
     */
    public static String getLevelName(int level) {
        String[] levelKeys = {
            "level.bottle.beginner",      // Level 1
            "level.trash.tamer",          // Level 2
            "level.compost.captain",      // Level 3
            "level.solar.scout",          // Level 4
            "level.green.goblin",         // Level 5
            "level.plastic.paladin",      // Level 6
            "level.worm.wrangler",        // Level 7
            "level.recycle.ranger",       // Level 8
            "level.eco.enchanter",        // Level 9
            "level.carbon.crusher",       // Level 10
            "level.led.legend",           // Level 11
            "level.sustainability.sorcerer", // Level 12
            "level.reusable.rebel",       // Level 13
            "level.veggie.viking",        // Level 14
            "level.planet.protector"      // Level 15
        };
        
        if (level >= 1 && level <= levelKeys.length) {
            return levelKeys[level - 1];
        }
        return "level.bottle.beginner"; // Default to first level
    }

    /**
     * Get next level name
     */
    public String getNextLevelName() {
        if (this.level >= 15) {
            return getLevelName(15); // Max level
        }
        return getLevelName(this.level + 1);
    }

    /**
     * Get points needed for next level
     */
    public int getPointsForNextLevel() {
        int[] levelThresholds = {50, 150, 300, 500, 750, 1050, 1400, 1800, 2250, 2750, 3300, 3900, 4550, 5250, 6000};
        
        if (this.level >= levelThresholds.length) {
            return levelThresholds[levelThresholds.length - 1]; // Max level reached
        }
        
        return levelThresholds[this.level - 1];
    }

    /**
     * Get points remaining until next level
     */
    public int getPointsRemainingForNextLevel() {
        int pointsForNextLevel = getPointsForNextLevel();
        int remaining = pointsForNextLevel - this.points;
        return Math.max(0, remaining);
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
        int[] levelThresholds = {50, 150, 300, 500, 750, 1050, 1400, 1800, 2250, 2750, 3300, 3900, 4550, 5250, 6000};
        
        if (this.level <= 1) {
            return 0;
        }
        
        if (this.level > levelThresholds.length) {
            return levelThresholds[levelThresholds.length - 1];
        }
        
        return levelThresholds[this.level - 2];
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

    public String getOauth2Provider() {
        return oauth2Provider;
    }

    public void setOauth2Provider(String oauth2Provider) {
        this.oauth2Provider = oauth2Provider;
    }

    public String getOauth2ProviderId() {
        return oauth2ProviderId;
    }

    public void setOauth2ProviderId(String oauth2ProviderId) {
        this.oauth2ProviderId = oauth2ProviderId;
    }

    public String getOauth2Name() {
        return oauth2Name;
    }

    public void setOauth2Name(String oauth2Name) {
        this.oauth2Name = oauth2Name;
    }

    public String getOauth2Picture() {
        return oauth2Picture;
    }

    public void setOauth2Picture(String oauth2Picture) {
        this.oauth2Picture = oauth2Picture;
    }
}