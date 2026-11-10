package northstar.school.com.school.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            String adminPass = System.getenv("ADMIN_PASSWORD");

            if (adminPass == null || adminPass.length() < 8) {
                System.out.println("No admin created. Set ADMIN_PASSWORD (at least 8 characters).");
                return;
            }

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@school.com");
            admin.setPassword(passwordEncoder.encode(adminPass));
            admin.setRole("ADMIN");
            userRepository.save(admin);

            System.out.println("Default admin created.");
        }
    }
}