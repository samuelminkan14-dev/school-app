package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class HRController {

    @Autowired
    private LeaveRequestRepository leaveRepo;

    @Autowired
    private TeacherRepository teacherRepo;

    @GetMapping("/hr")
    public String showHRPage(Model model) {
        model.addAttribute("leaves", leaveRepo.findAll());
        model.addAttribute("teachers", teacherRepo.findAll());
        return "hr";
    }

    @PostMapping("/requestLeave")
    public String requestLeave(@RequestParam Long teacherId, 
                               @RequestParam String startDate, 
                               @RequestParam String endDate, 
                               @RequestParam String reason) {
        Teacher teacher = teacherRepo.findById(teacherId).orElseThrow();
        
        LeaveRequest leave = new LeaveRequest();
        leave.setTeacher(teacher);
        leave.setStartDate(LocalDate.parse(startDate));
        leave.setEndDate(LocalDate.parse(endDate));
        leave.setReason(reason);
        leave.setStatus("PENDING");
        leaveRepo.save(leave);
        
        return "redirect:/hr";
    }

    @GetMapping("/approveLeave/{id}")
    public String approveLeave(@PathVariable Long id) {
        LeaveRequest leave = leaveRepo.findById(id).orElseThrow();
        leave.setStatus("APPROVED");
        leaveRepo.save(leave);
        return "redirect:/hr";
    }

    @GetMapping("/rejectLeave/{id}")
    public String rejectLeave(@PathVariable Long id) {
        LeaveRequest leave = leaveRepo.findById(id).orElseThrow();
        leave.setStatus("REJECTED");
        leaveRepo.save(leave);
        return "redirect:/hr";
    }
}