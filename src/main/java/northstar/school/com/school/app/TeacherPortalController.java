package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class TeacherPortalController {

    @Autowired private UserRepository userRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private TimetableRepository timetableRepository;
    @Autowired private QuizRepository quizRepository;
    @Autowired private ExamRepository examRepository;
    @Autowired private MarkRepository markRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private ResourceRepository resourceRepository;
    @Autowired private QuizSubmissionRepository submissionRepository;

    // --- DASHBOARD ---
    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(Model model) {
        Optional<Teacher> teacherOpt = getCurrentTeacher();
        if (teacherOpt.isEmpty()) return "redirect:/login?error=noteacher";

        Teacher teacher = teacherOpt.get();
        model.addAttribute("teacher", teacher);
        
        List<Classroom> myClasses = timetableRepository.findAll().stream()
                .filter(t -> t.getTeacher().getId().equals(teacher.getId()))
                .map(Timetable::getClassroom)
                .distinct()
                .toList();
        model.addAttribute("myClasses", myClasses);

        List<Quiz> myQuizzes = quizRepository.findAll().stream()
                .filter(q -> q.getTeacher().getId().equals(teacher.getId()))
                .toList();
        model.addAttribute("myQuizzes", myQuizzes);

        List<Resource> myResources = resourceRepository.findAll().stream()
                .filter(r -> r.getTeacher().getId().equals(teacher.getId()))
                .toList();
        model.addAttribute("myResources", myResources);

        return "teacher-dashboard";
    }

    // --- UPLOAD RESOURCE (WITH SECURITY CHECK) ---
    @PostMapping("/teacher/uploadResource")
    public String uploadResource(@RequestParam String title, 
                                 @RequestParam Long classId,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        Optional<Teacher> teacherOpt = getCurrentTeacher();
        if (teacherOpt.isEmpty()) return "redirect:/login";

        // --- SECURITY CHECK: PREVENT MALICIOUS UPLOADS ---
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file.");
            return "redirect:/teacher/dashboard";
        }

        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        // 1. Check File Type (Only allow PDF, Word, PowerPoint, Text)
        boolean isAllowed = (contentType != null) && (
                contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("application/vnd.ms-powerpoint") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
                contentType.equals("text/plain")
        );

        if (!isAllowed) {
            redirectAttributes.addFlashAttribute("error", "Security Error: Only PDF, Word, PPT, and TXT files are allowed.");
            return "redirect:/teacher/dashboard";
        }

        // 2. Check File Size (Max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("error", "Security Error: File size must be less than 10MB.");
            return "redirect:/teacher/dashboard";
        }

        try {
            Teacher teacher = teacherOpt.get();
            Classroom classroom = classroomRepository.findById(classId).orElseThrow();
            Resource resource = new Resource();
            resource.setTitle(title);
            resource.setFileName(fileName);
            resource.setFileType(contentType);
            resource.setData(file.getBytes());
            resource.setTeacher(teacher);
            resource.setClassroom(classroom);
            resourceRepository.save(resource);

            redirectAttributes.addFlashAttribute("successMessage", "Resource uploaded successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload file.");
        }
        return "redirect:/teacher/dashboard";
    }

    // --- DOWNLOAD RESOURCE ---
    @GetMapping("/teacher/downloadResource/{id}")
    public ResponseEntity<byte[]> downloadResource(@PathVariable Long id) {
        Resource res = resourceRepository.findById(id).orElseThrow();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + res.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(res.getFileType()))
                .body(res.getData());
    }

    // --- UPLOAD ASSESSMENT ---
    @PostMapping("/teacher/uploadQuiz")
    public String uploadQuiz(@RequestParam String title, 
                             @RequestParam String description, 
                             @RequestParam String dueDate,
                             @RequestParam String assessmentType,
                             @RequestParam Double maxScore,
                             @RequestParam Long classId) {
        Optional<Teacher> teacherOpt = getCurrentTeacher();
        if (teacherOpt.isEmpty()) return "redirect:/login";

        Teacher teacher = teacherOpt.get();
        Classroom classroom = classroomRepository.findById(classId).orElseThrow();

        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setDescription(description);
        quiz.setDueDate(LocalDate.parse(dueDate));
        quiz.setAssessmentType(assessmentType);
        quiz.setMaxScore(maxScore);
        quiz.setTeacher(teacher);
        quiz.setClassroom(classroom);
        quizRepository.save(quiz);

        return "redirect:/teacher/dashboard";
    }

    // --- GRADING QUIZZES ---
    @GetMapping("/teacher/gradeQuiz/{quizId}")
    public String showGradeQuiz(@PathVariable Long quizId, Model model) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        
        List<Student> students = studentRepository.findAll().stream()
                .filter(s -> s.getClassroom() != null && s.getClassroom().getId().equals(quiz.getClassroom().getId()))
                .toList();

        model.addAttribute("quiz", quiz);
        model.addAttribute("students", students);
        return "teacher-grade-quiz";
    }

    @PostMapping("/teacher/saveQuizGrade")
    public String saveQuizGrade(@RequestParam Long quizId, 
                                @RequestParam Long studentId, 
                                @RequestParam Double score) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        Student student = studentRepository.findById(studentId).orElseThrow();

        QuizSubmission submission = submissionRepository.findByQuizIdAndStudentId(quizId, studentId);
        if (submission == null) {
            submission = new QuizSubmission();
            submission.setQuiz(quiz);
            submission.setStudent(student);
        }
        
        submission.setScore(score);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus("GRADED");
        submissionRepository.save(submission);

        return "redirect:/teacher/gradeQuiz/" + quizId;
    }

    // --- CLASS ROSTER ---
    @GetMapping("/teacher/roster")
    public String showRoster(@RequestParam Long classId, Model model) {
        Classroom classroom = classroomRepository.findById(classId).orElseThrow();
        List<Student> students = studentRepository.findAll().stream()
                .filter(s -> s.getClassroom() != null && s.getClassroom().getId().equals(classId))
                .toList();
        model.addAttribute("students", students);
        model.addAttribute("classroom", classroom);
        return "teacher-roster";
    }

    // --- TIMETABLE ---
    @GetMapping("/teacher/timetable")
    public String showTimetable(Model model) {
        Optional<Teacher> teacherOpt = getCurrentTeacher();
        if (teacherOpt.isEmpty()) return "redirect:/login";
        List<Timetable> myTimetable = timetableRepository.findAll().stream()
                .filter(t -> t.getTeacher().getId().equals(teacherOpt.get().getId()))
                .toList();
        model.addAttribute("timetables", myTimetable);
        model.addAttribute("teacher", teacherOpt.get());
        return "teacher-timetable";
    }

    // --- ATTENDANCE ---
    @GetMapping("/teacher/attendance")
    public String markAttendance(@RequestParam Long classId, Model model) {
        Classroom classroom = classroomRepository.findById(classId).orElseThrow();
        List<Student> students = studentRepository.findAll().stream()
                .filter(s -> s.getClassroom() != null && s.getClassroom().getId().equals(classId))
                .toList();
        model.addAttribute("students", students);
        model.addAttribute("classroom", classroom);
        model.addAttribute("today", LocalDate.now());
        return "teacher-attendance";
    }

    @PostMapping("/teacher/saveAttendance")
    public String saveAttendance(@RequestParam Long studentId, @RequestParam String status, @RequestParam Long classId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        LocalDate today = LocalDate.now();
        Attendance existing = attendanceRepository.findByStudentIdAndDate(studentId, today);
        if (existing != null) {
            existing.setStatus(status);
            attendanceRepository.save(existing);
        } else {
            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setClassroom(student.getClassroom());
            attendance.setDate(today);
            attendance.setStatus(status);
            attendanceRepository.save(attendance);
        }
        return "redirect:/teacher/attendance?classId=" + classId;
    }

    // --- ENTER MARKS ---
    @GetMapping("/teacher/enterMarks")
    public String showEnterMarks(@RequestParam Long classId, Model model) {
        Classroom classroom = classroomRepository.findById(classId).orElseThrow();
        List<Student> students = studentRepository.findAll().stream()
                .filter(s -> s.getClassroom() != null && s.getClassroom().getId().equals(classId))
                .toList();
        model.addAttribute("students", students);
        model.addAttribute("classroom", classroom);
        model.addAttribute("exams", examRepository.findAll());
        model.addAttribute("subjects", subjectRepository.findAll());
        return "teacher-marks";
    }

    @PostMapping("/teacher/saveMark")
    public String saveMark(@RequestParam Long studentId, @RequestParam Long examId, @RequestParam Long subjectId,
                           @RequestParam Double score, @RequestParam Long classId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        Exam exam = examRepository.findById(examId).orElseThrow();
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();

        Mark mark = new Mark();
        mark.setStudent(student);
        mark.setExam(exam);
        mark.setSubject(subject);
        mark.setScore(score);
        mark.setMaxScore(100.0);
        markRepository.save(mark);
        return "redirect:/teacher/enterMarks?classId=" + classId;
    }

    private Optional<Teacher> getCurrentTeacher() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = userRepository.findByUsername(auth.getName());
        if (user.isPresent() && user.get().getLinkedId() != null) {
            return teacherRepository.findById(user.get().getLinkedId());
        }
        return Optional.empty();
    }
}