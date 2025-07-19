package com.greenlink.greenlink.config;

import com.greenlink.greenlink.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomOAuth2SuccessHandler oauth2SuccessHandler;

    public SecurityConfig(UserService userService, PasswordEncoder passwordEncoder, 
                        CustomAuthenticationSuccessHandler successHandler,
                        CustomAuthenticationFailureHandler failureHandler,
                        CustomOAuth2SuccessHandler oauth2SuccessHandler) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring security filter chain");
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/payment/webhook"))  // Enable CSRF but ignore for API endpoints and webhooks
            .authorizeHttpRequests(auth -> {
                logger.info("Configuring authorization rules");
                auth
                    .requestMatchers("/", "/register", "/login", "/css/**", "/js/**", "/images/**", "/files/**", "/webjars/**", "/error",
                                   "/educatie/**", "/marketplace/**", "/reciclare/**", "/despre/**", "/calculator/**", 
                                   "/provocari/**", "/contact/**", "/profile/**", "/auth-test/generate-password", 
                                   "/payment/webhook", "/payment/success", "/payment/cancel").permitAll()
                    .requestMatchers("/inbox/**").authenticated()
                    .anyRequest().authenticated();
            })
            .formLogin(form -> {
                logger.info("Configuring form login");
                form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .successHandler(successHandler)
                    .failureHandler(failureHandler)
                    .permitAll();
            })
            .oauth2Login(oauth2 -> {
                logger.info("Configuring OAuth2 login");
                oauth2
                    .loginPage("/login")
                    .successHandler(oauth2SuccessHandler)
                    .permitAll();
            })
            .logout(logout -> {
                logger.info("Configuring logout");
                logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/login?logout")
                    .clearAuthentication(true)
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll();
            })
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .expiredUrl("/login?expired"))
            .authenticationProvider(authenticationProvider());
        
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        logger.info("Creating DaoAuthenticationProvider");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        logger.info("Creating AuthenticationManager");
        return config.getAuthenticationManager();
    }
}