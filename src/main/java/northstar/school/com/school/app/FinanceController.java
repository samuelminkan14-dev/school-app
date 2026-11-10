package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class FinanceController {

    @Autowired private FeeRepository feeRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private PaymentService paymentService;
    @Autowired private SchoolSettingRepository settingRepository;

    @GetMapping("/finance")
    public String showFinancePage(Model model) {
        model.addAttribute("fees", feeRepository.findAll());
        model.addAttribute("payments", paymentRepository.findAll());
        model.addAttribute("classrooms", classroomRepository.findAll());
        model.addAttribute("students", studentRepository.findAll());
        return "finance";
    }

    @PostMapping("/createFee")
    public String createFee(@RequestParam String feeName, 
                            @RequestParam Double amount, 
                            @RequestParam Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId).orElseThrow();
        
        Fee fee = new Fee();
        fee.setFeeName(feeName);
        fee.setAmount(amount);
        fee.setClassroom(classroom);
        feeRepository.save(fee);
        
        return "redirect:/finance";
    }

    @PostMapping("/recordPayment")
    public String recordPayment(@RequestParam Long studentId, 
                                @RequestParam Long feeId, 
                                @RequestParam Double amountPaid,
                                @RequestParam String method,
                                RedirectAttributes redirectAttributes) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        Fee fee = feeRepository.findById(feeId).orElseThrow();
        SchoolSetting settings = settingRepository.findById(1L).orElse(new SchoolSetting());

        boolean success = paymentService.processPayment(amountPaid, student.getGuardian() != null ? student.getGuardian().getPhone() : "0000000000");

        if (success) {
            Payment payment = new Payment();
            payment.setStudent(student);
            payment.setFee(fee);
            payment.setAmountPaid(amountPaid);
            payment.setPaymentDate(LocalDate.now());
            payment.setMethod(method);
            payment.setReferenceNumber(paymentService.generateTransactionId());
            paymentRepository.save(payment);

            String email = student.getGuardian() != null ? student.getGuardian().getEmail() : null;
            paymentService.sendReceiptEmail(email, student.getFirstName(), amountPaid, payment.getReferenceNumber(), settings.getCurrency());

            redirectAttributes.addFlashAttribute("successMessage", "Payment successful! Receipt generated.");
            return "redirect:/receipt/" + payment.getId();
        }
        
        redirectAttributes.addFlashAttribute("error", "Payment failed. Please try again.");
        return "redirect:/finance";
    }

    @GetMapping("/receipt/{id}")
    public String showReceipt(@PathVariable Long id, Model model) {
        Payment payment = paymentRepository.findById(id).orElseThrow();
        model.addAttribute("payment", payment);
        return "receipt";
    }
}