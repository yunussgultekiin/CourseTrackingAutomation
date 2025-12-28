package org.example.coursetrackingautomation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
/**
 * Declares security-related Spring beans used by the application.
 */
public class SecurityBeans {

    @Bean
    /**
     * Creates the {@link PasswordEncoder} used for hashing user passwords.
     *
     * @return password encoder implementation
     */
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
