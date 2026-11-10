package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class MarkController {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private MarkRepository markRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    // --- EXISTING ROUTES ---
    @GetMapping("/marks")
    public String showMarksPage(Model model) {
        model.addAttribute("exams", examRepository.findAll());
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("marks", markRepository.findAll());
        return "marks";
    }

    @PostMapping("/createExam")
    public String createExam(@RequestParam String examName) {
        Exam exam = new Exam();
        exam.setExamName(examName);
        exam.setExamDate(LocalDate.now());
        examRepository.save(exam);
        return "redirect:/marks";
    }

    @PostMapping("/saveMark")
    public String saveMark(@RequestParam Long studentId, 
                           @RequestParam Long examId, 
                           @RequestParam Double score) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        Exam exam = examRepository.findById(examId).orElseThrow();

        Mark mark = new Mark();
        mark.setStudent(student);
        mark.setExam(exam);
        mark.setScore(score);
        markRepository.save(mark);

        return "redirect:/marks";
    }

    @GetMapping("/report-card/{id}")
    public String showReportCard(@PathVariable Long id, Model model) {
        Student student = studentRepository.findById(id).orElseThrow();
        List<Mark> studentMarks = markRepository.findByStudentId(id);
        
        double total = 0;
        for (Mark m : studentMarks) {
            total += m.getScore();
        }
        double average = studentMarks.isEmpty() ? 0 : total / studentMarks.size();

        model.addAttribute("student", student);
        model.addAttribute("marks", studentMarks);
        model.addAttribute("average", Math.round(average * 10.0) / 10.0);
        
        return "report-card";
    }

    // --- NEW GRADEBOOK ROUTE ---
    @GetMapping("/gradebook")
    public String showGradebook(@RequestParam(required = false) Long examId, Model model) {
        model.addAttribute("exams", examRepository.findAll());
        model.addAttribute("students", studentRepository.findAll());
        
        if (examId != null) {
            Exam exam = examRepository.findById(examId).orElseThrow();
            List<Mark> examMarks = markRepository.findByExamId(examId);
            model.addAttribute("selectedExam", exam);
            model.addAttribute("examMarks", examMarks);
        }
        
        return "gradebook";
    }
}