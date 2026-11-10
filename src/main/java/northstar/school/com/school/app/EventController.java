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
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/events")
    public String showEvents(Model model) {
        model.addAttribute("events", eventRepository.findAll());
        return "events";
    }

    @PostMapping("/addEvent")
    public String addEvent(@RequestParam String title,
                           @RequestParam String description,
                           @RequestParam String eventDate,
                           @RequestParam String location) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setEventDate(LocalDate.parse(eventDate));
        event.setLocation(location);
        eventRepository.save(event);

        return "redirect:/events";
    }

    @GetMapping("/deleteEvent/{id}")
    public String deleteEvent(@PathVariable Long id) {
        eventRepository.deleteById(id);
        return "redirect:/events";
    }
}