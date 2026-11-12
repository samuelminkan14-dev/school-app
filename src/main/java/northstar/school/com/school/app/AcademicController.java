package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AcademicController {

    @Autowired private ClassroomRepository classroomRepo;
    @Autowired private SubjectRepository subjectRepo;

    @GetMapping("/academics")
    public String showAcademics(Model model) {
        model.addAttribute("classes", classroomRepo.findAll());
        model.addAttribute("subjects", subjectRepo.findAll());
        return "academics";
    }

    @GetMapping("/academics/add-class")
    public String showAddClass() {
        return "academics-add-class";
    }

    @GetMapping("/academics/add-subject")
    public String showAddSubject() {
        return "academics-add-subject";
    }

    @PostMapping("/addClass")
    public String addClass(@RequestParam String className) {
        Classroom classroom = new Classroom();
        classroom.setClassName(className);
        classroomRepo.save(classroom);
        return "redirect:/academics";
    }

    @PostMapping("/addSubject")
    public String addSubject(@RequestParam String subjectName) {
        Subject subject = new Subject();
        subject.setSubjectName(subjectName);
        subjectRepo.save(subject);
        return "redirect:/academics";
    }
}