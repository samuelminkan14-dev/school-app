package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AcademicController {

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @GetMapping("/academics")
    public String showAcademics(Model model) {
        model.addAttribute("classes", classroomRepository.findAll());
        model.addAttribute("subjects", subjectRepository.findAll());
        return "academics";
    }

    @PostMapping("/addClass")
    public String addClass(@RequestParam String className) {
        Classroom classroom = new Classroom();
        classroom.setClassName(className);
        classroomRepository.save(classroom);
        return "redirect:/academics";
    }

    @PostMapping("/addSubject")
    public String addSubject(@RequestParam String subjectName) {
        Subject subject = new Subject();
        subject.setSubjectName(subjectName);
        subjectRepository.save(subject);
        return "redirect:/academics";
    }
}