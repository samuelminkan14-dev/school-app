package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class ParentPortalController {

    @Autowired private StudentRepository studentRepository;
    @Autowired private MarkRepository markRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private PaymentService paymentService;
    @Autowired private FeeRepository feeRepository;
    @Autowired private SchoolSettingRepository settingRepository;

    @GetMapping("/parent/dashboard")
    public String parentDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> user = userRepository.findByUsername(username);
        
        if (user.isPresent()) {
            Optional<Student> studentOpt = studentRepository.findAll().stream()
                    .filter(s -> s.getIndexNumber() != null && s.getIndexNumber().equals(username))
                    .findFirst();

            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                model.addAttribute("student", student);
                
                List<Mark> marks = markRepository.findByStudentId(student.getId());
                model.addAttribute("marks", marks);
                
                model.addAttribute("attendanceRecords", attendanceRepository.findByStudentId(student.getId()));
                model.addAttribute("payments", paymentRepository.findAll().stream()
                        .filter(p -> p.getStudent().getId().equals(student.getId())).toList());

                List<Announcement> announcements = announcementRepository.findAll();
                announcements.sort((a, b) -> b.getDatePosted().compareTo(a.getDatePosted()));
                model.addAttribute("announcements", announcements);

                return "parent-dashboard";
            }
        }
        return "redirect:/login?error=nostudent";
    }

    @PostMapping("/parent/pay-online")
    public String payOnline(@RequestParam Long studentId, 
                            @RequestParam Long feeId, 
                            @RequestParam Double amount,
                            RedirectAttributes redirectAttributes) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        Fee fee = feeRepository.findById(feeId).orElseThrow();
        SchoolSetting settings = settingRepository.findById(1L).orElse(new SchoolSetting());

        // Process MoMo Payment
        boolean success = paymentService.processPayment(amount, student.getGuardian().getPhone());
        
        if (success) {
            Payment payment = new Payment();
            payment.setStudent(student);
            payment.setFee(fee);
            payment.setAmountPaid(amount);
            payment.setPaymentDate(LocalDate.now());
            payment.setMethod("Online (MoMo)");
            payment.setReferenceNumber(paymentService.generateTransactionId());
            paymentRepository.save(payment);

            String email = student.getGuardian() != null ? student.getGuardian().getEmail() : null;
            paymentService.sendReceiptEmail(email, student.getFirstName(), amount, payment.getReferenceNumber(), settings.getCurrency());

            redirectAttributes.addFlashAttribute("successMessage", "Online payment successful!");
            
            // Redirect to Receipt
            return "redirect:/receipt/" + payment.getId();
        }
        
        redirectAttributes.addFlashAttribute("error", "Payment failed.");
        return "redirect:/parent/dashboard";
    }
}