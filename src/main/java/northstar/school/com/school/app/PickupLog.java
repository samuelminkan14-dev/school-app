package northstar.school.com.school.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PickupLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private LocalDateTime pickupTime;
    private String pickedUpBy; // Name of the person who picked them up

    public PickupLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public LocalDateTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalDateTime pickupTime) { this.pickupTime = pickupTime; }
    public String getPickedUpBy() { return pickedUpBy; }
    public void setPickedUpBy(String pickedUpBy) { this.pickedUpBy = pickedUpBy; }
}