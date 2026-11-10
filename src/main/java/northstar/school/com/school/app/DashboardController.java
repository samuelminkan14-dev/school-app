package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired private StudentRepository studentRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private MarkRepository markRepository;
    @Autowired private ProductSaleRepository productSaleRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 1. Basic Counts (Safe, returns 0 if empty)
        model.addAttribute("totalStudents", studentRepository.count());
        model.addAttribute("totalTeachers", teacherRepository.count());
        model.addAttribute("totalClasses", classroomRepository.count());

        // 2. Finance Stats (Safe from nulls and empty lists)
        double feesCollected = paymentRepository.findAll().stream()
                .filter(p -> p.getAmountPaid() != null)
                .mapToDouble(p -> p.getAmountPaid()).sum();
                
        double storeRevenue = productSaleRepository.findAll().stream()
                .filter(s -> s.getTotalAmount() != null)
                .mapToDouble(s -> s.getTotalAmount()).sum();
                
        model.addAttribute("totalCollected", feesCollected);
        model.addAttribute("storeRevenue", storeRevenue);

        // 3. Attendance Stats (Safe from divide-by-zero)
        long totalAttendance = attendanceRepository.count();
        long presentCount = attendanceRepository.findAll().stream()
                .filter(a -> "Present".equalsIgnoreCase(a.getStatus())).count();
        
        double attendanceRate = 0.0;
        if (totalAttendance > 0) {
            attendanceRate = ((double) presentCount / totalAttendance) * 100;
        }
        model.addAttribute("attendanceRate", Math.round(attendanceRate * 10.0) / 10.0);

        // 4. Marks Stats
        model.addAttribute("totalMarks", markRepository.count());

        // 5. Top Performing Students (Safe from empty marks)
        List<Mark> allMarks = markRepository.findAll();
        List<Student> topStudents = allMarks.stream()
                .filter(m -> m.getScore() != null && m.getStudent() != null) // Safety check
                .collect(java.util.stream.Collectors.groupingBy(
                        Mark::getStudent,
                        java.util.stream.Collectors.averagingDouble(Mark::getScore)
                ))
                .entrySet().stream()
                .sorted(java.util.Map.Entry.<Student, Double>comparingByValue().reversed())
                .limit(5)
                .map(java.util.Map.Entry::getKey)
                .toList();
                
        model.addAttribute("topStudents", topStudents);

        return "dashboard";
    }
}