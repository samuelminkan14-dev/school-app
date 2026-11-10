package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
public class PickupController {

    @Autowired private StudentRepository studentRepo;
    @Autowired private PickupLogRepository pickupRepo;
    @Autowired private NotificationService smsService;

    @GetMapping("/pickup")
    public String showPickupPage(Model model) {
        model.addAttribute("students", studentRepo.findAll());
        model.addAttribute("logs", pickupRepo.findAll());
        return "pickup";
    }

    @PostMapping("/recordPickup")
    public String recordPickup(@RequestParam Long studentId, 
                               @RequestParam String pickedUpBy) {
        Student student = studentRepo.findById(studentId).orElseThrow();

        PickupLog log = new PickupLog();
        log.setStudent(student);
        log.setPickedUpBy(pickedUpBy);
        log.setPickupTime(LocalDateTime.now());
        pickupRepo.save(log);

        // Send SMS to Parent
        if (student.getGuardian() != null && student.getGuardian().getPhone() != null) {
            String message = "Hello, " + student.getGuardian().getFullName() + ". Your child " + 
                             student.getFirstName() + " has been picked up by " + pickedUpBy + 
                             " at " + LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute() + ". Thank you.";
            smsService.sendSms(student.getGuardian().getPhone(), message);
        }

        return "redirect:/pickup";
    }
}