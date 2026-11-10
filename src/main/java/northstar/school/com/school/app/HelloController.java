package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @Autowired private AuditLogRepository auditRepo; // NEW: Audit Log

    @GetMapping("/")
    public String showDirectory(Model model) {
        model.addAttribute("students", studentRepository.findAll());
        return "hello";
    }

    @GetMapping("/enroll")
    public String showEnrollForm(Model model) {
        model.addAttribute("classrooms", classroomRepository.findAll());
        return "enroll";
    }

    @PostMapping("/addStudent")
    public String addStudent(@RequestParam String firstName, 
                             @RequestParam String lastName, 
                             @RequestParam String className, 
                             @RequestParam String guardianName,
                             @RequestParam String guardianPhone,
                             @RequestParam String guardianLocation,
                             @RequestParam("profilePic") MultipartFile profilePic,
                             RedirectAttributes redirectAttributes) {
        
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);

        if (profilePic != null && !profilePic.isEmpty()) {
            String contentType = profilePic.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Security Error: Only image files are allowed.");
                return "redirect:/enroll";
            }
            if (profilePic.getSize() > 2 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "Security Error: File size must be less than 2MB.");
                return "redirect:/enroll";
            }
            try {
                String base64Image = Base64.getEncoder().encodeToString(profilePic.getBytes());
                student.setProfilePicture("data:" + contentType + ";base64," + base64Image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Classroom classroom = getOrCreateClassroom(className);
        student.setClassroom(classroom);

        LocalDate now = LocalDate.now();
        String year = String.format("%02d", now.getYear() % 100);
        String month = String.format("%02d", now.getMonthValue());
        long countThisMonth = studentRepository.count() + 1; 
        String position = String.format("%02d", countThisMonth); 
        String indexNumber = "5250" + year + month + position;
        
        student.setIndexNumber(indexNumber);
        student.setAdmissionDate(LocalDate.now());
        studentRepository.save(student);

        Guardian guardian = new Guardian();
        guardian.setFullName(guardianName);
        guardian.setPhone(guardianPhone);
        guardian.setLocation(guardianLocation);
        guardian.setStudent(student);
        guardianRepository.save(guardian);

        User parentUser = new User();
        parentUser.setUsername(indexNumber);
        parentUser.setPassword(passwordEncoder.encode("parent123"));
        parentUser.setRole("PARENT");
        userRepository.save(parentUser);

        // --- AUDIT LOG: CREATE ---
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        auditRepo.save(new AuditLog(auth.getName(), "CREATE", "Enrolled student: " + firstName + " " + lastName + " (ID: " + indexNumber + ")"));

        redirectAttributes.addFlashAttribute("successMessage", 
            "Student enrolled! Index Number: " + indexNumber + ". Parent login created with password: parent123");

        return "redirect:/";
    }

    @GetMapping("/deleteStudent/{id}")
    public String deleteStudent(@PathVariable Long id) {
        Student student = studentRepository.findById(id).orElseThrow();
        
        // --- AUDIT LOG: DELETE ---
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        auditRepo.save(new AuditLog(auth.getName(), "DELETE", "Deleted student: " + student.getFirstName() + " " + student.getLastName()));

        studentRepository.deleteById(id);
        return "redirect:/";
    }

    // ... (Keep your editStudent, updateStudent, and getOrCreateClassroom methods exactly as they are)
    
    @GetMapping("/editStudent/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Student> student = studentRepository.findById(id);
        if (student.isPresent()) {
            model.addAttribute("student", student.get());
            model.addAttribute("classrooms", classroomRepository.findAll());
            return "edit-student";
        }
        return "redirect:/";
    }

    @PostMapping("/updateStudent")
    public String updateStudent(@RequestParam Long id,
                                @RequestParam String firstName, 
                                @RequestParam String lastName, 
                                @RequestParam String className) {
        Student student = studentRepository.findById(id).orElseThrow();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        Classroom classroom = getOrCreateClassroom(className);
        student.setClassroom(classroom);
        studentRepository.save(student);
        return "redirect:/";
    }

    private Classroom getOrCreateClassroom(String className) {
        Optional<Classroom> existingClass = classroomRepository.findAll().stream()
                .filter(c -> c.getClassName().equalsIgnoreCase(className.trim()))
                .findFirst();

        if (existingClass.isPresent()) {
            return existingClass.get();
        } else {
            Classroom classroom = new Classroom();
            classroom.setClassName(className.trim());
            return classroomRepository.save(classroom);
        }
    }
}