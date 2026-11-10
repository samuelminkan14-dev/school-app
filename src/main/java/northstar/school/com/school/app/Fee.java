package northstar.school.com.school.app;

import jakarta.persistence.*;

@Entity
public class Fee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String feeName; // e.g., "Term 1 Tuition"
    private Double amount;

    @ManyToOne
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    public Fee() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFeeName() { return feeName; }
    public void setFeeName(String feeName) { this.feeName = feeName; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Classroom getClassroom() { return classroom; }
    public void setClassroom(Classroom classroom) { this.classroom = classroom; }
}