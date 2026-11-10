package northstar.school.com.school.app;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentTransportRepository extends JpaRepository<StudentTransport, Long> {
}