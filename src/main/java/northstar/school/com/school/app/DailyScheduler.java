package northstar.school.com.school.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class DailyScheduler {

    @Autowired private StudentRepository studentRepo;
    @Autowired private NotificationService smsService;

    // This runs every day at 2:00 PM (14:00)
    // Format: second minute hour day month weekday
    @Scheduled(cron = "0 0 14 * * ?") 
    public void sendPickupReminders() {
        System.out.println("⏰ Running Daily Pickup Reminders...");
        
        List<Student> allStudents = studentRepo.findAll();

        for (Student student : allStudents) {
            // Check if the student is in Nursery or Lower Primary
            if (student.getClassroom() != null) {
                String className = student.getClassroom().getClassName().toLowerCase();
                
                if (className.contains("nursery") || className.contains("basic 1") || 
                    className.contains("basic 2") || className.contains("basic 3")) {
                    
                    if (student.getGuardian() != null && student.getGuardian().getPhone() != null) {
                        String message = "Reminder: School closes in 1 hour. Please prepare to pick up " + 
                                         student.getFirstName() + " (" + student.getClassroom().getClassName() + "). Thank you.";
                        smsService.sendSms(student.getGuardian().getPhone(), message);
                    }
                }
            }
        }
    }
}