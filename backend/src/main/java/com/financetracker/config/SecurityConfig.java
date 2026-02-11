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
                
                // Auth endpoints - no authentication required
                .antMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                
                // Swagger/OpenAPI docs
                .antMatchers("/api/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .antMatchers("/api/actuator/**").permitAll()
                
                // User endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/api/users/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/users/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/users/**").authenticated()
                
                // Transaction endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/api/transactions/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/transactions/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/transactions/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/transactions/**").authenticated()
                
                // Category endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/api/categories/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/categories/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/categories/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/categories/**").authenticated()
                
                // Budget endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/api/budgets/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/budgets/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/budgets/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/budgets/**").authenticated()
                
                // Recurring transaction endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/api/recurring-transactions/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/recurring-transactions/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/recurring-transactions/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/recurring-transactions/**").authenticated()
                
                // Analytics endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/api/analytics/**").authenticated()
                
                // Notification endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/api/notifications/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/notifications/**").authenticated()
                .antMatchers(HttpMethod.PATCH, "/api/notifications/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/notifications/**").authenticated()
                
                // File endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/api/files/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/files/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/files/**").authenticated()
                
                // Preference endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/api/preferences/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/preferences/**").authenticated()
                .antMatchers(HttpMethod.PATCH, "/api/preferences/**").authenticated()
                
                // Audit log endpoints - require authentication
                .antMatchers(HttpMethod.GET, "/api/audit-logs/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated();

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
