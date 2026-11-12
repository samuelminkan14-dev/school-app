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
public class StaffController {

    @Autowired private StaffRepository staffRepo;

    @GetMapping("/staff")
    public String showStaffList(Model model) {
        model.addAttribute("staffList", staffRepo.findAll());
        return "staff";
    }

    @GetMapping("/staff/add")
    public String showAddStaffForm() {
        return "staff-add";
    }

    @PostMapping("/addStaff")
    public String addStaff(@RequestParam String fullName, @RequestParam String role, @RequestParam String phone, @RequestParam Double salary) {
        Staff staff = new Staff();
        staff.setFullName(fullName);
        staff.setRole(role);
        staff.setPhone(phone);
        staff.setSalary(salary);
        staff.setHireDate(LocalDate.now());
        staffRepo.save(staff);
        return "redirect:/staff";
    }

    @GetMapping("/deleteStaff/{id}")
    public String deleteStaff(@PathVariable Long id) {
        staffRepo.deleteById(id);
        return "redirect:/staff";
    }
}