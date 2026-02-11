package com.financetracker.security;

import com.financetracker.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails implementation for Spring Security
 * Implements UserDetails interface to work with Spring Security
 */
@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    /**
     * Create CustomUserDetails from User entity
     */
    public static CustomUserDetails create(User user) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        String rolePrefix = "ROLE_";
        
        // Add authorities from user roles
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            user.getRoles().forEach(role -> 
                authorities.add(new SimpleGrantedAuthority(rolePrefix + role.getName()))
            );
        } else {
            // Default role if no roles assigned
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getIsActive() != null ? user.getIsActive() : true,
                true,
                true,
                user.getIsActive() != null ? user.getIsActive() : true
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
