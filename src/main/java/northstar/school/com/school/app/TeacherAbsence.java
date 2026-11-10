package northstar.school.com.school.app;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class TeacherAbsence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    private LocalDate date;
    private String reason;

    @ManyToOne
    @JoinColumn(name = "substitute_id")
    private Teacher substitute;

    // ... getters and setters ...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Teacher getSubstitute() { return substitute; }
    public void setSubstitute(Teacher substitute) { this.substitute = substitute; }
}