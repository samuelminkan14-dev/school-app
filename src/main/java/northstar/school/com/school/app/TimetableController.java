package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class TimetableController {

    @Autowired private TimetableRepository timetableRepo;
    @Autowired private ClassroomRepository classroomRepo;
    @Autowired private SubjectRepository subjectRepo;
    @Autowired private TeacherRepository teacherRepo;

    // 1. Show Timetable Page
    @GetMapping("/timetable")
    public String showTimetable(Model model) {
        model.addAttribute("timetables", timetableRepo.findAll());
        model.addAttribute("classrooms", classroomRepo.findAll());
        model.addAttribute("subjects", subjectRepo.findAll());
        model.addAttribute("teachers", teacherRepo.findAll());
        return "timetable";
    }

    // 2. Add Timetable Entry (Multiple Days)
    @PostMapping("/addTimetable")
    public String addTimetable(@RequestParam String className, 
                               @RequestParam String subjectName, 
                               @RequestParam Long teacherId,
                               @RequestParam List<String> days, 
                               @RequestParam String timeSlot) {
        
        Classroom classroom = getOrCreateClass(className);
        Subject subject = getOrCreateSubject(subjectName);

        // Loop through the selected days and save an entry for each
        for (String day : days) {
            Timetable t = new Timetable();
            t.setClassroom(classroom);
            t.setSubject(subject);
            t.setTeacher(teacherRepo.findById(teacherId).orElseThrow());
            t.setDayOfWeek(day);
            t.setTimeSlot(timeSlot);
            timetableRepo.save(t);
        }
        return "redirect:/timetable";
    }

    // 3. Show Edit Form
    @GetMapping("/editTimetable/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Timetable timetable = timetableRepo.findById(id).orElseThrow();
        model.addAttribute("timetable", timetable);
        model.addAttribute("teachers", teacherRepo.findAll());
        return "edit-timetable";
    }

    // 4. Save Updated Timetable Entry
    @PostMapping("/updateTimetable")
    public String updateTimetable(@RequestParam Long id,
                                  @RequestParam String className,
                                  @RequestParam String subjectName,
                                  @RequestParam Long teacherId,
                                  @RequestParam String dayOfWeek,
                                  @RequestParam String timeSlot) {
        Timetable t = timetableRepo.findById(id).orElseThrow();
        
        t.setClassroom(getOrCreateClass(className));
        t.setSubject(getOrCreateSubject(subjectName));
        t.setTeacher(teacherRepo.findById(teacherId).orElseThrow());
        t.setDayOfWeek(dayOfWeek);
        t.setTimeSlot(timeSlot);
        
        timetableRepo.save(t);
        return "redirect:/timetable";
    }

    // 5. Delete Timetable Entry
    @GetMapping("/deleteTimetable/{id}")
    public String deleteTimetable(@PathVariable Long id) {
        timetableRepo.deleteById(id);
        return "redirect:/timetable";
    }

    // --- HELPER METHODS ---
    private Classroom getOrCreateClass(String name) {
        Optional<Classroom> existing = classroomRepo.findAll().stream()
                .filter(c -> c.getClassName().equalsIgnoreCase(name.trim())).findFirst();
        return existing.orElseGet(() -> {
            Classroom c = new Classroom();
            c.setClassName(name.trim());
            return classroomRepo.save(c);
        });
    }

    private Subject getOrCreateSubject(String name) {
        Optional<Subject> existing = subjectRepo.findAll().stream()
                .filter(s -> s.getSubjectName().equalsIgnoreCase(name.trim())).findFirst();
        return existing.orElseGet(() -> {
            Subject s = new Subject();
            s.setSubjectName(name.trim());
            return subjectRepo.save(s);
        });
    }
}