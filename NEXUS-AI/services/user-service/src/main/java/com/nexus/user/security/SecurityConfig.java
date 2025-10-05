package com.nexus.user.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // Dla @PreAuthorize na methods (z pierwszej)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint JwtAuthenticationEntryPoint;  // Custom 401 handler

    @Autowired
    private JwtAuthenticationFilter JwtAuthenticationFilter;  // JWT filter z poprzedniej (połączonej)

    @Autowired
    private UserDetailsService userDetailsService;  // Standard nazwa (z repo; DAO auth)

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // BCrypt dla hash passwords (z obu)
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {  // DAO dla UserDetails (z pierwszej; lepszy niż Builder)
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {  // Modern (z pierwszej)
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {  // Modern lambda style (z pierwszej/druga)
        http
                .cors(cors -> cors.disable())  // Disable CORS (z pierwszej; global config osobno jeśli potrzeba)
                .csrf(csrf -> csrf.disable())  // Disable CSRF (lambda z drugiej; stateless JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Stateless (z obu)
                .exceptionHandling(exceptions -> exceptions  // Custom 401 (z obu)
                        .authenticationEntryPoint(JwtAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(authz -> authz  // Pełne matchery (z pierwszej + auth/swagger z drugiej)
                        .requestMatchers("/api/auth/**").permitAll()  // Login/register
                        .requestMatchers("/api/public/**").permitAll()  // Public endpoints
                        .requestMatchers("/actuator/health").permitAll()  // Health checks
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // Swagger (z obu)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")  // Role ADMIN
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")  // Role USER/ADMIN
                        .anyRequest().authenticated()  // Reszta wymaga auth (z obu)
                );

        // Register our authentication provider to use UserDetailsService + PasswordEncoder
        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(JwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  // JWT filter przed default (z obu)

        return http.build();
    }
}
