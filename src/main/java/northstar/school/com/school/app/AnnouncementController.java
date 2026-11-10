package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AnnouncementController {

    @Autowired
    private AnnouncementRepository announcementRepo;

    @GetMapping("/announcements")
    public String showAnnouncements(Model model) {
        // Show newest first
        List<Announcement> announcements = announcementRepo.findAll();
        announcements.sort((a, b) -> b.getDatePosted().compareTo(a.getDatePosted()));
        
        model.addAttribute("announcements", announcements);
        return "announcements";
    }

    @PostMapping("/addAnnouncement")
    public String addAnnouncement(@RequestParam String title,
                                  @RequestParam String message,
                                  @RequestParam String priority) {
        Announcement ann = new Announcement();
        ann.setTitle(title);
        ann.setMessage(message);
        ann.setPriority(priority);
        ann.setDatePosted(LocalDate.now());
        announcementRepo.save(ann);
        return "redirect:/announcements";
    }

    @GetMapping("/deleteAnnouncement/{id}")
    public String deleteAnnouncement(@PathVariable Long id) {
        announcementRepo.deleteById(id);
        return "redirect:/announcements";
    }
}