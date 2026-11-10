package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuditController {

    @Autowired private AuditLogRepository auditRepo;

    @GetMapping("/audit-logs")
    public String showAuditLogs(Model model) {
        // Sort logs by newest first
        model.addAttribute("logs", auditRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp")));
        return "audit-logs";
    }
}