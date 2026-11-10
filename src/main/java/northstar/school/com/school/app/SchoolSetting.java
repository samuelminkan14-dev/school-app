package northstar.school.com.school.app;

import jakarta.persistence.*;

@Entity
public class SchoolSetting {
    @Id
    private Long id = 1L; // We only ever use ID 1 for the global settings

    private String schoolName;
    private String address;
    private String phone;
    private String email;
    private String currency; // e.g., GHS, USD, NGN

    public SchoolSetting() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}