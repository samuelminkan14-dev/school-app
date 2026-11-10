package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GuardianController {

    @Autowired private GuardianRepository guardianRepo;
    @Autowired private StudentRepository studentRepo;

    // 1. Show the Guardian Directory
    @GetMapping("/guardians")
    public String showGuardians(Model model) {
        model.addAttribute("guardians", guardianRepo.findAll());
        model.addAttribute("students", studentRepo.findAll());
        return "guardians";
    }

    // 2. Create a new Guardian (if needed from a separate form)
    @PostMapping("/addGuardian")
    public String addGuardian(@RequestParam String fullName, 
                              @RequestParam String relationship, 
                              @RequestParam String phone, 
                              @RequestParam Long studentId) {
        Student student = studentRepo.findById(studentId).orElseThrow();
        
        Guardian guardian = new Guardian();
        guardian.setFullName(fullName);
        guardian.setRelationship(relationship);
        guardian.setPhone(phone);
        guardian.setStudent(student);
        guardianRepo.save(guardian);
        
        return "redirect:/guardians";
    }

    // 3. UPDATE the Guardian Details (This is the fix!)
    @PostMapping("/updateGuardian")
    public String updateGuardian(@RequestParam Long id,
                                 @RequestParam String fullName,
                                 @RequestParam String phone,
                                 @RequestParam String location) {
        Guardian guardian = guardianRepo.findById(id).orElseThrow();
        guardian.setFullName(fullName);
        guardian.setPhone(phone);
        guardian.setLocation(location);
        guardianRepo.save(guardian);
        return "redirect:/guardians";
    }

    // 4. DELETE a Guardian
    @GetMapping("/deleteGuardian/{id}")
    public String deleteGuardian(@PathVariable Long id) {
        guardianRepo.deleteById(id);
        return "redirect:/guardians";
    }
}