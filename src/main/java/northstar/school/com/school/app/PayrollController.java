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

    // 1. Show Payroll History & Expenses
    @GetMapping("/payroll")
    public String showPayroll(Model model) {
        model.addAttribute("payrolls", payrollRepo.findAll());
        model.addAttribute("expenses", expenseRepo.findAll());
        return "payroll";
    }

    // 2. Show Generate Payslip Form
    @GetMapping("/payroll/add-payslip")
    public String showAddPayslipForm(Model model) {
        model.addAttribute("teachers", teacherRepo.findAll());
        return "payroll-add-payslip";
    }

    // 3. Show Add Expense Form
    @GetMapping("/payroll/add-expense")
    public String showAddExpenseForm() {
        return "payroll-add-expense";
    }

    // 4. Handle Payslip Submission
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

    // 5. Handle Expense Submission
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