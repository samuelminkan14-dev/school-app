package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, 
                               @RequestParam String email, 
                               @RequestParam String password,
                               Model model) {
        
        // 1. Check if username exists
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username is already taken. Please choose another one.");
            return "login"; // Return to login page with error
        }

        // 2. Check if email exists
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "An account with this email already exists.");
            return "login";
        }

        // 3. Create the user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("STUDENT"); // Default role
        userRepository.save(user);

        return "redirect:/login?success";
    }

    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot-password";
    }
}