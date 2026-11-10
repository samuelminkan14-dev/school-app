package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@ControllerAdvice // <--- This makes the settings available on EVERY page
public class SettingsController {

    @Autowired
    private SchoolSettingRepository settingRepository;

    // This automatically adds the settings to every page
    @ModelAttribute("globalSettings")
    public SchoolSetting getGlobalSettings() {
        return settingRepository.findById(1L).orElseGet(() -> {
            SchoolSetting defaultSettings = new SchoolSetting();
            defaultSettings.setSchoolName("My School");
            defaultSettings.setCurrency("GHS");
            return defaultSettings;
        });
    }

    @GetMapping("/settings")
    public String showSettings(Model model) {
        model.addAttribute("settings", getGlobalSettings());
        return "settings";
    }

    @PostMapping("/saveSettings")
    public String saveSettings(@RequestParam String schoolName,
                               @RequestParam String address,
                               @RequestParam String phone,
                               @RequestParam String email,
                               @RequestParam String currency,
                               RedirectAttributes redirectAttributes) {
        SchoolSetting settings = settingRepository.findById(1L).orElse(new SchoolSetting());
        settings.setSchoolName(schoolName);
        settings.setAddress(address);
        settings.setPhone(phone);
        settings.setEmail(email);
        settings.setCurrency(currency);
        
        settingRepository.save(settings);
        redirectAttributes.addFlashAttribute("successMessage", "Settings saved successfully!");
        
        return "redirect:/settings";
    }
}