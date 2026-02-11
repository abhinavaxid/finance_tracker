package com.financetracker.repository;

import com.financetracker.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entity operations
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Find role by name
     */
    Optional<Role> findByName(String name);

    /**
     * Check if role exists
     */
    boolean existsByName(String name);
}
