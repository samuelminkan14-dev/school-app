package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TransportController {

    @Autowired
    private BusRouteRepository busRouteRepository;

    @Autowired
    private StudentTransportRepository studentTransportRepository;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/transport")
    public String showTransportPage(Model model) {
        model.addAttribute("routes", busRouteRepository.findAll());
        model.addAttribute("transports", studentTransportRepository.findAll());
        model.addAttribute("students", studentRepository.findAll());
        return "transport";
    }

    @PostMapping("/addRoute")
    public String addRoute(@RequestParam String routeName, 
                           @RequestParam String driverName, 
                           @RequestParam String vehiclePlate) {
        BusRoute route = new BusRoute();
        route.setRouteName(routeName);
        route.setDriverName(driverName);
        route.setVehiclePlate(vehiclePlate);
        busRouteRepository.save(route);
        return "redirect:/transport";
    }

    @PostMapping("/assignTransport")
    public String assignTransport(@RequestParam Long studentId, 
                                  @RequestParam Long routeId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        BusRoute route = busRouteRepository.findById(routeId).orElseThrow();

        StudentTransport transport = new StudentTransport();
        transport.setStudent(student);
        transport.setBusRoute(route);
        studentTransportRepository.save(transport);

        return "redirect:/transport";
    }
}