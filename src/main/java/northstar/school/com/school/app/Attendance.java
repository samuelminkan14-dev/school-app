package northstar.school.com.school.app;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "date"}))
public class Attendance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    // This links the attendance record to the specific class
    @ManyToOne
    @JoinColumn(name = "class_id")
    private Classroom classroom;

    private LocalDate date;
    private String status;

    public Attendance() {}

    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public Student getStudent() { 
        return student; 
    }
    
    public void setStudent(Student student) { 
        this.student = student; 
    }
    
    public Classroom getClassroom() { 
        return classroom; 
    }
    
    public void setClassroom(Classroom classroom) { 
        this.classroom = classroom; 
    }
    
    public LocalDate getDate() { 
        return date; 
    }
    
    public void setDate(LocalDate date) { 
        this.date = date; 
    }
    
    public String getStatus() { 
        return status; 
    }
    
    public void setStatus(String status) { 
        this.status = status; 
    }
}