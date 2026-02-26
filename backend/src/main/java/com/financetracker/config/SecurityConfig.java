package com.financetracker.config;

import com.financetracker.security.JwtAuthenticationEntryPoint;
import com.financetracker.security.JwtAuthenticationFilter;
import com.financetracker.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security Configuration
 * Configures authentication, authorization, and JWT-based security
 * Production-grade configuration with proper CORS, CSRF, and access control
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    /**
     * Create JWT authentication filter bean
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * Password encoder bean - BCrypt for production
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication manager bean
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://localhost:4200",
                "https://localhost:3000",
                "https://localhost:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configure authentication manager
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
    }

    /**
     * Configure HTTP security
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors()
                .and()
            .csrf()
                .disable()
            .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeRequests()
                // Public endpoints
                .antMatchers("/", "/favicon.ico", 
                    "/**/*.png", "/**/*.gif", "/**/*.svg", "/**/*.jpg", "/**/*.jpeg",
                    "/**/*.html", "/**/*.css", "/**/*.js", "/**/*.map").permitAll()
                
                // Auth endpoints - no authentication required (without /api prefix since context-path=/api)
                .antMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .antMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .antMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                .antMatchers(HttpMethod.GET, "/auth/session").permitAll()
                .antMatchers(HttpMethod.POST, "/auth/change-password").authenticated()
                .antMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                
                // Swagger/OpenAPI docs
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .antMatchers("/actuator/**").permitAll()
                
                // User endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/users/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/users/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/users/**").authenticated()
                
                // Transaction endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/transactions/**").authenticated()
                .antMatchers(HttpMethod.POST, "/transactions/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/transactions/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/transactions/**").authenticated()
                
                // Category endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/categories/**").authenticated()
                .antMatchers(HttpMethod.POST, "/categories/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/categories/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/categories/**").authenticated()
                
                // Budget endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/budgets/**").authenticated()
                .antMatchers(HttpMethod.POST, "/budgets/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/budgets/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/budgets/**").authenticated()
                
                // Recurring transaction endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/recurring-transactions/**").authenticated()
                .antMatchers(HttpMethod.POST, "/recurring-transactions/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/recurring-transactions/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/recurring-transactions/**").authenticated()
                
                // Analytics endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/analytics/**").authenticated()
                
                // Notification endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/notifications/**").authenticated()
                .antMatchers(HttpMethod.POST, "/notifications/**").authenticated()
                .antMatchers(HttpMethod.PATCH, "/notifications/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/notifications/**").authenticated()
                
                // File endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/files/**").authenticated()
                .antMatchers(HttpMethod.POST, "/files/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/files/**").authenticated()
                
                // Preference endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/preferences/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/preferences/**").authenticated()
                .antMatchers(HttpMethod.PATCH, "/preferences/**").authenticated()
                
                // Audit log endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/audit-logs/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated();

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
