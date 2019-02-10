package ws.manu.reservation_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ws.manu.reservation_system.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByAgentIdAndStartGreaterThanEqualAndEndLessThanEqual(int agentId, LocalDateTime dateStart, LocalDateTime dateEnd);

    List<Reservation> findByUserId(Long userId);
}
