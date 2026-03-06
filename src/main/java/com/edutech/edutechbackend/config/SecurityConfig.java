package com.edutech.edutechbackend.config;


import com.edutech.edutechbackend.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
// ↑ tells Spring: this class contains @Bean methods
//   run them at startup and register results in Application Context

@EnableWebSecurity
// ↑ activates Spring Security
//   tells Spring Boot: use MY config instead of auto-defaults
//   without this → random password in console, form login etc

@RequiredArgsConstructor
// ↑ Lombok generates constructor for all final fields
//   Spring uses this constructor to inject dependencies
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    // ↑ our bridge between Student entity and Spring Security
    //   injected by Spring via constructor
    //   used in: authenticationProvider() bean

    private final JwtAuthFilter jwtAuthFilter;
    // ↑ our JWT filter
    //   injected by Spring via constructor
    //   registered in: securityFilterChain() bean

    // ── BEAN 1: Password Encoder ─────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
        //                               ↑
        //                        cost factor = 10
        //                        means 2^10 = 1024 hashing rounds
        //                        ~100ms per hash
        //                        slow enough to prevent brute force
        //                        fast enough for normal use
        //
        // Used in:
        //   AuthService.register():
        //     passwordEncoder.encode("rawPassword")
        //     → "$2a$10$randomsalt+hashedpassword"
        //     → store this in DB
        //
        //   DaoAuthenticationProvider (login):
        //     passwordEncoder.matches("rawInput", "$2a$10$storedHash")
        //     → true or false
    }

    // ── BEAN 2: Authentication Provider ──────────────────────────────────
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);
        //                               ↑
        //   constructor takes UserDetailsService (Spring Security 6.4+ way)
        //   tells provider HOW TO FIND users:
        //     → call loadUserByUsername(email)
        //     → hit PostgreSQL
        //     → return UserDetails

        provider.setPasswordEncoder(passwordEncoder());
        //        ↑
        //   tells provider HOW TO VERIFY passwords:
        //     → BCrypt.matches(rawInput, storedHash)
        //     → true = login success
        //     → false = BadCredentialsException

        return provider;
        // ↑ fully configured provider stored in Application Context
        //   used by AuthenticationManager during login
    }

    // ── BEAN 3: Authentication Manager ───────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        //                             ↑
        //   Spring automatically injects this
        //   it knows about all registered AuthenticationProviders
        //   including our DaoAuthenticationProvider above

        return config.getAuthenticationManager();
        // ↑ returns an AuthenticationManager that:
        //   → accepts UsernamePasswordAuthenticationToken
        //   → delegates to our DaoAuthenticationProvider
        //   → which calls loadUserByUsername()
        //   → which calls passwordEncoder.matches()
        //
        // We @Autowire this in AuthService:
        //   authManager.authenticate(
        //     new UsernamePasswordAuthenticationToken(email, password)
        //   )
        //   → success: returns Authentication object
        //   → failure: throws BadCredentialsException
    }

    // ── BEAN 4: Security Filter Chain (THE RULE BOOK) ────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        //                    ↑
        //   Spring injects HttpSecurity
        //   it's the builder for all security rules

        http
                // ── 1. Disable CSRF ───────────────────────────────────────────
                .csrf(AbstractHttpConfigurer::disable)
                //    ↑
                //    CSRF attacks use cookies/sessions to forge requests
                //    We use JWT (not cookies) → immune to CSRF
                //    Disabling removes unnecessary overhead
                //    Every POST/PUT/DELETE would need CSRF token otherwise

                // ── 2. Route Rules ────────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth

                                .requestMatchers("/api/auth/**").permitAll()
                                //               ↑               ↑
                                // matches any URL              no auth required
                                // starting with /api/auth/     completely open
                                //
                                // covers:
                                //   /api/auth/register → open ✅
                                //   /api/auth/login    → open ✅
                                //   /api/auth/refresh  → open ✅ (when we add it)

                                .anyRequest().authenticated()
                        //  ↑            ↑
                        // everything    must have valid JWT token
                        // else          if no token → 401
                        //               if invalid token → 401
                        //               if valid token → allow ✅
                        //
                        // covers:
                        //   /api/students/**  → protected
                        //   /api/tests/**     → protected
                        //   /api/subjects/**  → protected
                        //   everything else   → protected
                )

                // ── 3. Session Policy ─────────────────────────────────────────
                .sessionManagement(session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        //                                           ↑
                        //   STATELESS = never create HTTP sessions
                        //   server stores NOTHING between requests
                        //   every request is independent
                        //   client must send JWT on every request
                        //
                        //   Benefits:
                        //     → no session memory usage
                        //     → works across multiple servers
                        //     → infinitely scalable
                )

                // ── 4. Register our Authentication Provider ───────────────────
                .authenticationProvider(authenticationProvider())
                //                      ↑
                //   tells Spring Security:
                //   "use MY DaoAuthenticationProvider"
                //   "which uses MY CustomUserDetailsService"
                //   "which uses MY BCryptPasswordEncoder"
                //
                //   without this → Spring uses default provider
                //   which knows nothing about our database

                // ── 5. Register JWT Filter ────────────────────────────────────
                .addFilterBefore(
                        jwtAuthFilter,
                        //↑ our filter to add
                        UsernamePasswordAuthenticationFilter.class
                        //↑ add BEFORE this Spring built-in filter
                        //
                        // Filter chain order result:
                        //   ... → JwtAuthFilter → UsernamePasswordAuthFilter → ...
                        //
                        // Why BEFORE?
                        //   JwtAuthFilter runs first
                        //   → validates token
                        //   → sets SecurityContext
                        //   Then Spring's filter runs
                        //   → sees SecurityContext already set
                        //   → does nothing (our auth already handled)
                        //
                        //   If AFTER:
                        //   → Spring's filter runs first
                        //   → SecurityContext empty
                        //   → might block before our filter gets to run ❌
                );

        return http.build();
        // ↑ build and return the fully configured SecurityFilterChain
        //   Spring registers this as the active security configuration
    }
}