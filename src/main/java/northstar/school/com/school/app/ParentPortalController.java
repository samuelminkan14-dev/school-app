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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ParentPortalController {

    @Autowired private StudentRepository studentRepository;
    @Autowired private MarkRepository markRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private PaymentService paymentService;
    @Autowired private FeeRepository feeRepository;
    @Autowired private SchoolSettingRepository settingRepository;
    @Autowired private FeedbackRepository feedbackRepository;

    private Student getLoggedInStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return studentRepository.findAll().stream()
                .filter(s -> s.getIndexNumber() != null && s.getIndexNumber().equals(username))
                .findFirst().orElse(null);
    }

    // --- 1. OVERVIEW DASHBOARD ---
    @GetMapping("/parent/dashboard")
    public String parentDashboard(Model model, jakarta.servlet.http.HttpSession session) {
        Student student = getLoggedInStudent();
        if (student == null) return "redirect:/login?error=nostudent";
        
        model.addAttribute("student", student);
        model.addAttribute("attendanceRecords", attendanceRepository.findByStudentId(student.getId()));
        
        List<Announcement> announcements = announcementRepository.findAll();
        announcements.sort((a, b) -> b.getDatePosted().compareTo(a.getDatePosted()));
        model.addAttribute("announcements", announcements);
        
        session.removeAttribute("welcomeMessage");
        return "parent-dashboard";
    }

    // --- 2. EXAM RESULTS ---
    @GetMapping("/parent/exams")
    public String parentExams(Model model) {
        Student student = getLoggedInStudent();
        if (student == null) return "redirect:/login";
        
        model.addAttribute("student", student);
        model.addAttribute("marks", markRepository.findByStudentId(student.getId()));
        return "parent-exams";
    }

    @GetMapping("/parent/payments")
    public String parentPayments(Model model) {
        Student student = getLoggedInStudent();
        if (student == null) return "redirect:/login";
        
        model.addAttribute("student", student);
        model.addAttribute("payments", paymentRepository.findAll().stream()
                .filter(p -> p.getStudent() != null && p.getStudent().getId().equals(student.getId())).toList());
        
        if (student.getClassroom() != null) {
            model.addAttribute("classFees", feeRepository.findAll().stream()
                    .filter(f -> f.getClassroom() != null && f.getClassroom().getId().equals(student.getClassroom().getId()))
                    .toList());
        } else {
            model.addAttribute("classFees", new java.util.ArrayList<>());
        }
        return "parent-payments";
    }

    // --- 4. PAYMENT PROCESSING ---
    @PostMapping("/parent/pay-online")
    public String payOnline(@RequestParam Long studentId, @RequestParam Long feeId, @RequestParam Double amount, RedirectAttributes redirectAttributes) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        Fee fee = feeRepository.findById(feeId).orElseThrow();
        SchoolSetting settings = settingRepository.findById(1L).orElse(new SchoolSetting());

        boolean success = paymentService.processPayment(amount, student.getGuardian().getPhone());
        
        if (success) {
            Payment payment = new Payment();
            payment.setStudent(student);
            payment.setFee(fee);
            payment.setAmountPaid(amount);
            payment.setPaymentDate(LocalDate.now());
            payment.setMethod("Online (Paystack/MoMo)");
            payment.setReferenceNumber(paymentService.generateTransactionId());
            paymentRepository.save(payment);

            String email = student.getGuardian() != null ? student.getGuardian().getEmail() : null;
            paymentService.sendReceiptEmail(email, student.getFirstName(), amount, payment.getReferenceNumber(), settings.getCurrency());

            redirectAttributes.addFlashAttribute("successMessage", "Payment successful!");
            return "redirect:/receipt/" + payment.getId();
        }
        
        redirectAttributes.addFlashAttribute("error", "Payment failed.");
        return "redirect:/parent/payments";
    }

    // --- 5. FEEDBACK ---
    @GetMapping("/parent/feedback")
    public String parentFeedback(Model model) {
        Student student = getLoggedInStudent();
        if (student == null) return "redirect:/login";
        model.addAttribute("student", student);
        return "parent-feedback";
    }

    @PostMapping("/parent/submit-feedback")
    public String submitFeedback(@RequestParam String parentName, @RequestParam String message, RedirectAttributes redirectAttributes) {
        Feedback feedback = new Feedback();
        feedback.setParentName(parentName);
        feedback.setMessage(message);
        feedback.setSubmittedAt(LocalDateTime.now());
        feedback.setRead(false);
        feedbackRepository.save(feedback);

        redirectAttributes.addFlashAttribute("feedbackSuccess", "Your message has been sent to the Admin.");
        return "redirect:/parent/feedback";
    }
}