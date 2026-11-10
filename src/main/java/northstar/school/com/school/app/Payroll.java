package northstar.school.com.school.app;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @Column(name = "`month`") // Backticks prevent SQL keyword errors
    private String month; 
    
    private Double basicSalary;
    private Double allowances;
    private Double deductions;
    private LocalDate paymentDate;

    public Payroll() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public Double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(Double basicSalary) { this.basicSalary = basicSalary; }
    public Double getAllowances() { return allowances; }
    public void setAllowances(Double allowances) { this.allowances = allowances; }
    public Double getDeductions() { return deductions; }
    public void setDeductions(Double deductions) { this.deductions = deductions; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    // Helper method to calculate Net Pay automatically
    public Double getNetPay() {
        if (basicSalary == null) return 0.0;
        Double allow = allowances == null ? 0.0 : allowances;
        Double deduct = deductions == null ? 0.0 : deductions;
        return basicSalary + allow - deduct;
    }
}