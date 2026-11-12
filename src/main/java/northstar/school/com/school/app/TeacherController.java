package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class TeacherController {

    @Autowired private TeacherRepository teacherRepo;
    @Autowired private ClassroomRepository classroomRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private TimetableRepository timetableRepo;

    @GetMapping("/teachers")
    public String listTeachers(Model model) {
        model.addAttribute("teachers", teacherRepo.findAll());
        return "teachers";
    }

    @GetMapping("/teachers/add")
    public String showAddTeacherForm(Model model) {
        model.addAttribute("classrooms", classroomRepo.findAll());
        return "teacher-add";
    }

    @PostMapping("/addTeacher")
    public String addTeacher(@RequestParam String firstName, 
                             @RequestParam String lastName, 
                             @RequestParam String subject,
                             @RequestParam(required = false) Long classroomId) {
        Teacher teacher = new Teacher();
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setSubject(subject);
        
        // --- GENERATE UNIQUE STAFF ID ---
        LocalDate now = LocalDate.now();
        String year = String.format("%02d", now.getYear() % 100); // e.g. 26
        String month = String.format("%02d", now.getMonthValue()); // e.g. 11
        String day = String.format("%02d", now.getDayOfMonth());   // e.g. 12
        
        // 1. Create the base ID (5052 + YY + MM + DD)
        String baseStaffId = "5052" + year + month + day;
        
        // 2. Check if this base ID already exists in the database
        boolean exists = teacherRepo.findAll().stream()
                .anyMatch(t -> baseStaffId.equals(t.getStaffId()));
        
        // 3. Determine the final ID
        String finalStaffId;
        if (exists) {
            // If it exists, add the total count of teachers + 1 to the end to make it unique
            finalStaffId = baseStaffId + String.format("%02d", teacherRepo.count() + 1);
        } else {
            finalStaffId = baseStaffId;
        }
        
        teacher.setStaffId(finalStaffId);
        
        if (classroomId != null) {
            teacher.setClassroom(classroomRepo.findById(classroomId).orElse(null));
        }
        
        teacherRepo.save(teacher);
        return "redirect:/teachers";
    }

    @GetMapping("/deleteTeacher/{id}")
    public String deleteTeacher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean hasUser = userRepo.findAll().stream()
                    .anyMatch(u -> u.getLinkedId() != null && u.getLinkedId().equals(id));
            
            boolean hasTimetable = timetableRepo.findAll().stream()
                    .anyMatch(t -> t.getTeacher() != null && t.getTeacher().getId().equals(id));

            if (hasUser || hasTimetable) {
                redirectAttributes.addFlashAttribute("error", "Cannot delete teacher. They are linked to a User account or assigned to a Timetable.");
                return "redirect:/teachers";
            }

            teacherRepo.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting the teacher.");
        }
        return "redirect:/teachers";
    }
}