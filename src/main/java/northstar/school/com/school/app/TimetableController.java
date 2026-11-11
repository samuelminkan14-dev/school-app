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

    @GetMapping("/timetable")
    public String showTimetable(Model model) {
        model.addAttribute("timetables", timetableRepo.findAll());
        model.addAttribute("classrooms", classroomRepo.findAll());
        model.addAttribute("subjects", subjectRepo.findAll());
        model.addAttribute("teachers", teacherRepo.findAll());
        return "timetable";
    }

    @PostMapping("/addTimetable")
    public String addTimetable(@RequestParam String className, 
                               @RequestParam String subjectName, 
                               @RequestParam Long teacherId,
                               @RequestParam List<String> days, 
                               @RequestParam String timeSlot) {
        
        // 1. Find or Create the Classroom
        Optional<Classroom> existingClass = classroomRepo.findAll().stream()
                .filter(c -> c.getClassName().equalsIgnoreCase(className.trim()))
                .findFirst();
        Classroom classroom = existingClass.orElseGet(() -> {
            Classroom newClass = new Classroom();
            newClass.setClassName(className.trim());
            return classroomRepo.save(newClass);
        });

        // 2. Find or Create the Subject
        Optional<Subject> existingSubject = subjectRepo.findAll().stream()
                .filter(s -> s.getSubjectName().equalsIgnoreCase(subjectName.trim()))
                .findFirst();
        Subject subject = existingSubject.orElseGet(() -> {
            Subject newSubject = new Subject();
            newSubject.setSubjectName(subjectName.trim());
            return subjectRepo.save(newSubject);
        });

        // 3. Save the schedule for each selected day
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

    @GetMapping("/deleteTimetable/{id}")
    public String deleteTimetable(@PathVariable Long id) {
        timetableRepo.deleteById(id);
        return "redirect:/timetable";
    }
}