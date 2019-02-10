package ws.manu.reservation_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ws.manu.reservation_system.model.NextAvailableReservation;
import ws.manu.reservation_system.model.ServiceTier;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NextAvailableReservationRepository extends JpaRepository<NextAvailableReservation, Integer> {
    List<NextAvailableReservation> findOneByNextAvailableReservation(LocalDateTime dateTime);

    List<NextAvailableReservation> findTop5ByServiceTierOrderByNextAvailableReservationAsc(ServiceTier tier);

    @Transactional
    void deleteAllByAgentId(Integer id);
}
