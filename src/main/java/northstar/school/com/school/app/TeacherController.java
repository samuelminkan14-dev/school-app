package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;

@Controller
public class TeacherController {

    @Autowired
    private TeacherRepository teacherRepository;

    @GetMapping("/teachers")
    public String listTeachers(Model model) {
        model.addAttribute("teachers", teacherRepository.findAll());
        return "teachers";
    }

    @PostMapping("/addTeacher")
    public String addTeacher(@RequestParam String firstName, 
                             @RequestParam String lastName, 
                             @RequestParam String subject) {
        Teacher teacher = new Teacher();
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setSubject(subject);
        teacherRepository.save(teacher);
        return "redirect:/teachers";
    }

    @GetMapping("/deleteTeacher/{id}")
    public String deleteTeacher(@PathVariable Long id) {
        teacherRepository.deleteById(id);
        return "redirect:/teachers";
    }

    @GetMapping("/editTeacher/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Teacher> teacher = teacherRepository.findById(id);
        if (teacher.isPresent()) {
            model.addAttribute("teacher", teacher.get());
            return "edit-teacher";
        }
        return "redirect:/teachers";
    }

    @PostMapping("/updateTeacher")
    public String updateTeacher(@RequestParam Long id,
                                @RequestParam String firstName, 
                                @RequestParam String lastName, 
                                @RequestParam String subject) {
        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setSubject(subject);
        teacherRepository.save(teacher);
        return "redirect:/teachers";
    }
}