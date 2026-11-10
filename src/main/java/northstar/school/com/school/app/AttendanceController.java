package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Optional;

@Controller
public class AttendanceController {

    // 🔴 PUT YOUR MNOTIFY API KEY HERE LATER 🔴
    private static final String MNOTIFY_API_KEY = "YOUR_MNOTIFY_API_KEY";

    @Autowired private StudentRepository studentRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private GuardianRepository guardianRepository;

    @GetMapping("/attendance")
    public String showAttendance(Model model) {
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("today", LocalDate.now());
        return "attendance";
    }

    @PostMapping("/saveAttendance")
    public String saveAttendance(@RequestParam Long studentId, 
                                 @RequestParam String status) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        
        // Check if attendance already exists for today
        Attendance attendance = attendanceRepository.findByStudentIdAndDate(studentId, LocalDate.now());
        if (attendance == null) {
            attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setDate(LocalDate.now());
        }
        attendance.setStatus(status);
        attendanceRepository.save(attendance);

        // NEW: If Absent, Send SMS Alert to Parent
        if ("Absent".equalsIgnoreCase(status)) {
            sendAbsenceAlert(student);
        }
        
        return "redirect:/attendance";
    }

    // Helper method to send the SMS
    private void sendAbsenceAlert(Student student) {
        // Find the guardian linked to this student
        Optional<Guardian> guardianOpt = guardianRepository.findAll().stream()
                .filter(g -> g.getStudent().getId().equals(student.getId()))
                .findFirst();

        if (guardianOpt.isPresent()) {
            Guardian guardian = guardianOpt.get();
            String phone = guardian.getPhone();
            String message = "Hello " + guardian.getFullName() + ", this is an alert from the school. Your child " + student.getFirstName() + " " + student.getLastName() + " was marked ABSENT today (" + LocalDate.now() + ").";

            // If API key is set, send SMS
            if (!MNOTIFY_API_KEY.equals("YOUR_MNOTIFY_API_KEY")) {
                sendSms(phone, message);
            } else {
                System.out.println("⚠️ SMS Skipped (No API Key). Would have sent to " + phone + ": " + message);
            }
        }
    }

    // The mNotify logic (same as in HelloController)
    private void sendSms(String phoneNumber, String message) {
        try {
            String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
            if (cleanPhone.startsWith("0")) {
                cleanPhone = "233" + cleanPhone.substring(1);
            }
            String escapedMessage = message.replace("\"", "\\\"");
            String jsonPayload = String.format("""
                {
                    "recipient": ["%s"],
                    "sender": "SchoolMS",
                    "message": "%s",
                    "is_schedule": false
                }
                """, cleanPhone, escapedMessage);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mnotify.com/api/sms/quick?key=" + MNOTIFY_API_KEY))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("✅ Absence SMS sent to " + cleanPhone);
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
        }
    }
}