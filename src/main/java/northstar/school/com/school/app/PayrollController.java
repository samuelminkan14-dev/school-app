package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class PayrollController {

    @Autowired private PayrollRepository payrollRepo;
    @Autowired private ExpenseRepository expenseRepo;
    @Autowired private TeacherRepository teacherRepo;

    @GetMapping("/payroll")
    public String showPayroll(Model model) {
        model.addAttribute("payrolls", payrollRepo.findAll());
        model.addAttribute("expenses", expenseRepo.findAll());
        model.addAttribute("teachers", teacherRepo.findAll());
        return "payroll";
    }

    // 1. Manually Generate Payslip for one Teacher
    @PostMapping("/generatePayslip")
    public String generatePayslip(@RequestParam Long teacherId,
                                  @RequestParam String month,
                                  @RequestParam Double basicSalary,
                                  @RequestParam Double allowances,
                                  @RequestParam Double deductions) {
        Teacher teacher = teacherRepo.findById(teacherId).orElseThrow();
        
        Payroll payroll = new Payroll();
        payroll.setTeacher(teacher);
        payroll.setMonth(month);
        payroll.setBasicSalary(basicSalary);
        payroll.setAllowances(allowances);
        payroll.setDeductions(deductions);
        payroll.setPaymentDate(LocalDate.now());
        payrollRepo.save(payroll);
        
        return "redirect:/payroll";
    }

    // 2. AUTOMATED PAYROLL: Generate for ALL teachers at once
    @PostMapping("/generateMonthlyPayroll")
    public String generateMonthlyPayroll() {
        LocalDate now = LocalDate.now();
        String currentMonth = now.getMonth().toString() + " " + now.getYear();

        // Check if payroll already exists for this month to avoid duplicates
        boolean exists = payrollRepo.findAll().stream().anyMatch(p -> p.getMonth().equals(currentMonth));
        if (exists) {
            return "redirect:/payroll?alreadyGenerated";
        }

        // Loop through every teacher and create a payslip
        for (Teacher t : teacherRepo.findAll()) {
            Double baseSalary = 2000.0; // Default base salary

            Payroll p = new Payroll();
            p.setTeacher(t);
            p.setMonth(currentMonth);
            p.setBasicSalary(baseSalary);
            p.setAllowances(0.0);
            p.setDeductions(0.0);
            p.setPaymentDate(LocalDate.now());
            payrollRepo.save(p);
        }
        return "redirect:/payroll?generated";
    }

    // 3. Record School Expense
    @PostMapping("/addExpense")
    public String addExpense(@RequestParam String description, @RequestParam Double amount) {
        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setExpenseDate(LocalDate.now());
        expenseRepo.save(expense);
        return "redirect:/payroll";
    }
}