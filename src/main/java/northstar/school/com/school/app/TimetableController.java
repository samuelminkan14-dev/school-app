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

    // 1. Show the Timetable Page
    @GetMapping("/timetable")
    public String showTimetable(Model model) {
        model.addAttribute("timetables", timetableRepo.findAll());
        model.addAttribute("classrooms", classroomRepo.findAll());
        model.addAttribute("subjects", subjectRepo.findAll());
        model.addAttribute("teachers", teacherRepo.findAll());
        return "timetable";
    }

    // 2. Handle Form Submission (Select OR Type new)
    @PostMapping("/addTimetable")
    public String addTimetable(@RequestParam String className, 
                               @RequestParam String subjectName, 
                               @RequestParam Long teacherId,
                               @RequestParam List<String> days, 
                               @RequestParam String timeSlot) {
        
        // --- A. Handle the Class ---
        // Check if the class exists in the database (case-insensitive)
        Optional<Classroom> existingClass = classroomRepo.findAll().stream()
                .filter(c -> c.getClassName().equalsIgnoreCase(className.trim()))
                .findFirst();
        
        Classroom classroom;
        if (existingClass.isPresent()) {
            classroom = existingClass.get();
        } else {
            // If it doesn't exist, create a brand new one
            classroom = new Classroom();
            classroom.setClassName(className.trim());
            classroomRepo.save(classroom);
        }

        // --- B. Handle the Subject ---
        // Check if the subject exists in the database (case-insensitive)
        Optional<Subject> existingSubject = subjectRepo.findAll().stream()
                .filter(s -> s.getSubjectName().equalsIgnoreCase(subjectName.trim()))
                .findFirst();
        
        Subject subject;
        if (existingSubject.isPresent()) {
            subject = existingSubject.get();
        } else {
            // If it doesn't exist, create a brand new one
            subject = new Subject();
            subject.setSubjectName(subjectName.trim());
            subjectRepo.save(subject);
        }

        // --- C. Save the Schedule for each selected day ---
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

    // 3. Delete a Timetable Entry
    @GetMapping("/deleteTimetable/{id}")
    public String deleteTimetable(@PathVariable Long id) {
        timetableRepo.deleteById(id);
        return "redirect:/timetable";
    }
}