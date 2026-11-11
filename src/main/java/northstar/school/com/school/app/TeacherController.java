package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TeacherController {

    @Autowired private TeacherRepository teacherRepo;
    @Autowired private ClassroomRepository classroomRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private TimetableRepository timetableRepo;

    @GetMapping("/teachers")
    public String listTeachers(Model model) {
        model.addAttribute("teachers", teacherRepo.findAll());
        model.addAttribute("classrooms", classroomRepo.findAll());
        return "teachers";
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