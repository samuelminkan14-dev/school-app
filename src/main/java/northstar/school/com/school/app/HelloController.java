package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;

@Controller
public class HelloController {

    @Autowired private StudentRepository studentRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private GuardianRepository guardianRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // 1. SHOW STUDENT LIST
    @GetMapping("/")
    public String showDirectory(Model model) {
        model.addAttribute("students", studentRepository.findAll());
        return "hello";
    }

    // 2. SHOW ENROLL FORM
    @GetMapping("/enroll")
    public String showEnrollForm(Model model) {
        model.addAttribute("classrooms", classroomRepository.findAll());
        return "enroll";
    }

    // 3. HANDLE ENROLLMENT
    @PostMapping("/addStudent")
    public String addStudent(@RequestParam String firstName, 
                             @RequestParam String lastName, 
                             @RequestParam String className, 
                             @RequestParam String guardianName,
                             @RequestParam String guardianPhone,
                             @RequestParam String guardianLocation,
                             @RequestParam(value = "profilePic", required = false) MultipartFile profilePic,
                             RedirectAttributes redirectAttributes) {
        
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);

        // Handle Profile Picture
        if (profilePic != null && !profilePic.isEmpty()) {
            try {
                String base64Image = Base64.getEncoder().encodeToString(profilePic.getBytes());
                student.setProfilePicture("data:" + profilePic.getContentType() + ";base64," + base64Image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Handle Class
        Optional<Classroom> existingClass = classroomRepository.findAll().stream()
                .filter(c -> c.getClassName().equalsIgnoreCase(className.trim())).findFirst();
        Classroom classroom = existingClass.orElseGet(() -> {
            Classroom newClass = new Classroom();
            newClass.setClassName(className.trim());
            return classroomRepository.save(newClass);
        });
        student.setClassroom(classroom);

        // Generate Index Number
        LocalDate now = LocalDate.now();
        String year = String.format("%02d", now.getYear() % 100);
        String month = String.format("%02d", now.getMonthValue());
        long countThisMonth = studentRepository.count() + 1; 
        String position = String.format("%02d", countThisMonth); 
        String indexNumber = "5250" + year + month + position;
        
        student.setIndexNumber(indexNumber);
        student.setAdmissionDate(LocalDate.now());
        studentRepository.save(student);

        // Create Guardian
        Guardian guardian = new Guardian();
        guardian.setFullName(guardianName);
        guardian.setPhone(guardianPhone);
        guardian.setLocation(guardianLocation);
        guardian.setStudent(student);
        guardianRepository.save(guardian);

        // Create Parent User
        User parentUser = new User();
        parentUser.setUsername(indexNumber);
        parentUser.setPassword(passwordEncoder.encode("parent123"));
        parentUser.setRole("PARENT");
        parentUser.setLinkedId(student.getId());
        userRepository.save(parentUser);

        redirectAttributes.addFlashAttribute("successMessage", 
            "Student enrolled! Index Number: " + indexNumber + ". Parent login created with password: parent123");

        return "redirect:/";
    }

    @GetMapping("/deleteStudent/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentRepository.deleteById(id);
        return "redirect:/";
    }
}