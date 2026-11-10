package northstar.school.com.school.app;

import jakarta.persistence.*;

@Entity
public class StudentTransport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private BusRoute busRoute;

    public StudentTransport() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public BusRoute getBusRoute() { return busRoute; }
    public void setBusRoute(BusRoute busRoute) { this.busRoute = busRoute; }
}