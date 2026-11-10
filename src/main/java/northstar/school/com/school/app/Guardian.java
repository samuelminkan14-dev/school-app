package northstar.school.com.school.app;

import jakarta.persistence.*;

@Entity
public class Guardian {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String relationship;
    private String phone;
    private String location;
    private String email; // <--- THIS WAS MISSING

    @OneToOne
    @JoinColumn(name = "student_id")
    private Student student;

    public Guardian() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    // NEW: Getter and Setter for Email
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
}