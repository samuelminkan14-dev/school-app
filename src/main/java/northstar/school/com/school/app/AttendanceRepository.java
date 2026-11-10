package northstar.school.com.school.app;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    // Needed for the Parent Portal
    List<Attendance> findByStudentId(Long studentId);
    
    // Needed for the Attendance Page (so teachers don't mark a student twice in one day)
    Attendance findByStudentIdAndDate(Long studentId, LocalDate date);
}