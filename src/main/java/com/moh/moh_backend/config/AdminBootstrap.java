package com.moh.moh_backend.config;

import com.moh.moh_backend.Enum.UserRole;
import com.moh.moh_backend.model.User;
import com.moh.moh_backend.repository.UserRepository;
import com.moh.moh_backend.util.PasswordHashService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the first ADMIN account, since /auth/register is admin-only.
 * Runs only when BOOTSTRAP_ADMIN_USERNAME, BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD are all set
 * and the database has no active admin yet; otherwise it does nothing.
 * Remove the variables again after the first start.
 */
@Component
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordHashService hashService;
    private final String username;
    private final String email;
    private final String password;

    public AdminBootstrap(UserRepository userRepository,
                          PasswordHashService hashService,
                          @Value("${BOOTSTRAP_ADMIN_USERNAME:}") String username,
                          @Value("${BOOTSTRAP_ADMIN_EMAIL:}") String email,
                          @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String password) {
        this.userRepository = userRepository;
        this.hashService = hashService;
        this.username = username.trim();
        this.email = email.trim();
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return;
        }
        boolean hasActiveAdmin = userRepository.findByRole(UserRole.ADMIN).stream()
                .anyMatch(u -> !Boolean.FALSE.equals(u.getIsActive()));
        if (hasActiveAdmin) {
            System.out.println("Admin bootstrap skipped: an active admin already exists. "
                    + "Remove the BOOTSTRAP_ADMIN_* variables.");
            return;
        }
        if (password.length() < 8) {
            throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD must be at least 8 characters");
        }
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Admin bootstrap: username or email is already used by another account");
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPasswordHash(hashService.hashPassword(password));
        admin.setRole(UserRole.ADMIN);
        admin.setIsActive(true);
        userRepository.save(admin);
        System.out.println("Admin bootstrap: created admin '" + username + "'. Remove the BOOTSTRAP_ADMIN_* variables now.");
    }
}
