package com.__2107027.mini_marketplace.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseConstraintFix {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixDatabaseConstraints() {
        try {
            // Drop the old constraint if it exists
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
            System.out.println("✓ Dropped old users_role_check constraint");
            
            // Add the new constraint with correct values
            jdbcTemplate.execute("ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('USER', 'ADMIN'))");
            System.out.println("✓ Added new users_role_check constraint with correct values");
            
        } catch (Exception e) {
            System.err.println("Error fixing database constraints: " + e.getMessage());
            // Don't throw exception to allow application to start
        }
    }
}
