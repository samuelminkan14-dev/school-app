package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    @Autowired private UserRepository userRepo;
    @Autowired private TeacherRepository teacherRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- USER MANAGEMENT ROUTES ---
    @GetMapping("/users")
    public String showUsers(Model model) {
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("teachers", teacherRepo.findAll()); 
        return "users";
    }

    @PostMapping("/addUser")
    public String addUser(@RequestParam String username, 
                          @RequestParam String password, 
                          @RequestParam String role,
                          @RequestParam(required = false) Long linkedId) {
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        
        if ("TEACHER".equals(role) && linkedId != null) {
            user.setLinkedId(linkedId);
        }
        
        userRepo.save(user);
        return "redirect:/users";
    }

    @GetMapping("/deleteUser/{id}")
    public String deleteUser(@org.springframework.web.bind.annotation.PathVariable Long id) {
        userRepo.deleteById(id);
        return "redirect:/users";
    }
}